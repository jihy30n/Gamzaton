package com.example.demo.user.jwt;

import javax.security.sasl.AuthenticationException;

public class JwtExpiredException extends AuthenticationException {
    public JwtExpiredException(String message) {

        super(message);
    }
}