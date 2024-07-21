package com.example.demo.user.service.jwt;

import com.example.demo.core.error.ErrorCode;
import com.example.demo.core.error.exeption.NotFoundException;
import com.example.demo.core.error.exeption.UnAuthorizedException;
import com.example.demo.user.entity.UserEntity;
import com.example.demo.user.repo.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.codec.binary.Hex;
import com.google.gson.JsonObject;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final UserRepository userRepository;

    private final CustomUserDetailsService customUserDetailsService;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.accessExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshExpiration}")
    private long refreshTokenValidTime;

    @Value("${jwt.aesKey}")
    private String aesKey;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long id, String role) {
        try {
            return this.createToken(id, role, accessTokenValidTime, "access");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createRefreshToken(Long id, String role) {
        try {
            return this.createToken(id, role, refreshTokenValidTime, "refresh");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createToken(Long id, String role, long tokenValid, String tokenType) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pk", id);
        jsonObject.addProperty("role", role);
        jsonObject.addProperty("tokenType", tokenType);


        Claims claims = Jwts.claims().subject(encrypt(jsonObject.toString())).build();
        Date date = new Date();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(date)
                .expiration(new Date(date.getTime() + tokenValid))
                .signWith(getSigningKey())
                .compact();
    }

    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader("authorization", "Bearer " + accessToken);
    }

    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        response.setHeader("refreshToken", "Bearer " + refreshToken);
    }


    private String encrypt(String plainToken) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec IV = new IvParameterSpec(aesKey.substring(0, 16).getBytes());

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secretKeySpec, IV);

        byte[] encryptionByte = c.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));

        return Hex.encodeHexString(encryptionByte);
    }

    private String decrypt(String encodeText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec IV = new IvParameterSpec(aesKey.substring(0, 16).getBytes());

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secretKeySpec, IV);

        byte[] decodeByte = Hex.decodeHex(encodeText);

        return new String(c.doFinal(decodeByte), StandardCharsets.UTF_8);

    }

    public Long extractId(String token) throws Exception {
        JsonElement id = extraValue(token).get("pk");
        return id.getAsLong();
    }

    public String extractRole(String token) throws Exception {
        JsonElement role = extraValue(token).get("role");
        return role.getAsString();
    }

    public String extractMemberId(String token) throws Exception {
        Long id = extractId(token);
        String role = extractRole(token); // 토큰에서 역할 추출

        Optional<UserEntity> UserIdOptional = userRepository.findById(id);

            if (UserIdOptional.isEmpty()) {
                throw new NotFoundException("사용자를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);

            }
       else {
            throw new UnAuthorizedException("권한이 없는 사용자입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }


    private JsonObject extraValue(String token) throws Exception {
        String subject = extraAllClaims(token).getSubject();
        String decrypted = decrypt(subject);
        JsonObject jsonObject = new Gson().fromJson(decrypted, JsonObject.class);
        return jsonObject;
    }


    public boolean isAdmin(String token) throws Exception {
        String role = extractRole(token);
        return role.equals("admin");
    }

    private Claims extraAllClaims(String token) {
        return getParser()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractTokenType(String token) throws Exception {
        JsonElement tokenType = extraValue(token).get("tokenType");
        return String.valueOf(tokenType);
    }

    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return request.getHeader("authorization").substring(7);
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("refreshToken");
        if (request.getHeader("refreshToken") != null)
            return request.getHeader("refreshToken").substring(7);
        return null;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) throws Exception {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(extractMemberId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = extraAllClaims(refreshToken);
            return !claims.getExpiration().before(new Date());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT claims string is empty");
        }
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            Claims claims = extraAllClaims(accessToken);

            return !claims.getExpiration().before(new Date());
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "AccessToken is Expired");
        } catch (UnsupportedJwtException ex) {
            throw new UnsupportedJwtException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT claims string is empty");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
        try {
            this.validateRefreshToken(refreshToken);
            Long id = findUserIdByToken(refreshToken);
            Optional<UserEntity> user = userRepository.findById(id);
            return createAccessToken(id, String.valueOf(user.get().getUserRole()));
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return ErrorCode.EXPIRED_ACCESS_TOKEN.getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long findUserIdByToken(String token) throws Exception {
        String accessTokenType = extractTokenType(token);

        if ("access".equals(accessTokenType)) {
            throw new UnAuthorizedException("AccessToken은 사용 할 수 없습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        return token == null ? null : userRepository.findById(extractId(token))
                .orElseThrow(() -> new NotFoundException("토큰에 해당하는 ID값을 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).getId();
    }

    public Optional<UserEntity> extractIdByRequest(HttpServletRequest request) throws Exception {
        String userToken = resolveAccessToken(request);
        Long tokenId = extractId(userToken);
        return userRepository.findById(tokenId);
    }
}
