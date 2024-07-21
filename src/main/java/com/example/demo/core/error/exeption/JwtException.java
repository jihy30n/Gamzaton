package com.example.demo.core.error.exeption;

import com.example.demo.core.error.ErrorCode;
import lombok.Getter;

@Getter
public class JwtException extends BusinessException{
    public JwtException(String message, ErrorCode errorCode){
        super(message,errorCode);
    }
}
