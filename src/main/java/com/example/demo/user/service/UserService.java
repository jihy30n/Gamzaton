package com.example.demo.user.service;


import com.example.demo.core.error.exeption.UnAuthorizedException;
import com.example.demo.user.dto.LoginRequestDto;
import com.example.demo.user.dto.LoginResponseDto;
import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.repo.UserRepository;
import com.example.demo.user.service.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static com.example.demo.core.error.ErrorCode.ACCESS_DENIED_EXCEPTION;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {

        if (!userRepository.existsByEmailAndDeleted(requestDto.getEmail(), false)) {
            if (!userRepository.existsByEmail(requestDto.getEmail())) {
                return LoginResponseDto.builder()
                        .responseCode("2001")   // 회원이 아닐 경우
                        .build();
            } else if (userRepository.existsByEmailAndDeletedIsTrue(requestDto.getEmail())) {
                return LoginResponseDto.builder()
                        .responseCode("2002")   // 탈퇴한 회원인 경우
                        .build();
            }
        }

        UserEntity userEntity = userRepository.findByEmail(requestDto.getEmail());

        //패스워드 다를 때
        if (!passwordEncoder.matches(requestDto.getPassword(), userEntity.getPassword())) {
            throw new UnAuthorizedException("401", ACCESS_DENIED_EXCEPTION);
        }

        try {
            this.setJwtTokenInHeader(userEntity.getId(), String.valueOf(userEntity.getUserRole()), response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return LoginResponseDto.builder()
                .responseCode("200")
                .build();
    }

    private void setJwtTokenInHeader(Long id, String role, HttpServletResponse response) throws Exception {
        String accessToken = jwtProvider.createAccessToken(id, role);
        String refreshToken = jwtProvider.createRefreshToken(id, role);

        jwtProvider.setHeaderAccessToken(response, accessToken);
        jwtProvider.setHeaderRefreshToken(response, refreshToken);

    }
    public void reissueToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        jwtProvider.validateRefreshToken(refreshToken);

        String newAccessToken = jwtProvider.reissueAccessToken(refreshToken, response);

        jwtProvider.setHeaderAccessToken(response, newAccessToken);

    }



}
