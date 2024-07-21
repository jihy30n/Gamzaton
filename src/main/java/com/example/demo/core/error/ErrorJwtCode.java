package com.example.demo.core.error;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorJwtCode {

    INVALID_JWT_TOKEN(4001, "Invalid JWT token"),
    JWT_TOKEN_EXPIRED(4002, "JWT token has expired"),
    UNSUPPORTED_JWT_TOKEN(4003, "JWT token is unsupported"),
    EMPTY_JWT_CLAIMS(4004, "JWT claims string is empty"),
    JWT_SIGNATURE_MISMATCH(4005, "JWT signature does not match"),
    JWT_COMPLEX_ERROR(4006, "JWT Complex error"),
    ACCESS_TOKEN_EXPIRED(1006, "ACCESS TOKEN has expired"),
    EXPIRED_REFRESH_TOKEN(1008,"1006 EXPIRED REFRESH TOKEN"),
    INVALID_JWT_FORMAT(1001,"1001 INVALID JWT FORMAT"),
    INVALID_VALUE(1004,"1004 INVALID VALUE"),
    RUNTIME_EXCEPTION(1005,"1005 RUNTIME EXCEPTION"),
    EXPIRED_ACCESS_TOKEN(1007,"1007 EXPIRED ACCESS TOKEN"),

    REFRESH_TOKEN_EXPIRED(1007, "REFRESH TOKEN has expired");

    private final int code;
    private final String message;

}