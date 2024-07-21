package com.example.demo.user.service;


import com.example.demo.core.error.exeption.UnAuthorizedException;
import com.example.demo.user.jwt.JwtProvider;
import com.example.demo.user.custom.UserRole;
import com.example.demo.user.dto.LoginRequestDto;
import com.example.demo.user.dto.LoginResponseDto;
import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.repo.UserRepository;
import com.example.demo.user.service.jwt.RedisService;
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
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
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

        UserEntity userEntity = userRepository.findByEmail(requestDto.getEmail()).orElseThrow();

        //패스워드 다를 때
        if (!passwordEncoder.matches(requestDto.getPassword(), userEntity.getPassword())) {
            throw new UnAuthorizedException("401", ACCESS_DENIED_EXCEPTION);
        }

        this.setJwtTokenInHeader(requestDto.getEmail(), response);

        return LoginResponseDto.builder()
                .responseCode("200")
                .build();
    }

    public void setJwtTokenInHeader(String email, HttpServletResponse response) {
        UserRole userRole = userRepository.findByEmail(email).get().getUserRole();

        String accessToken = jwtProvider.createAccessToken(email, userRole);
        String refreshToken = jwtProvider.createRefreshToken(email, userRole);


        jwtProvider.setHeaderAT(response, accessToken);
        jwtProvider.setHeaderRT(response, refreshToken);

        redisService.setValues(refreshToken, email);
    }


}
