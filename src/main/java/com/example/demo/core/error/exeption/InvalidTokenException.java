package com.example.demo.core.error.exeption;

import com.example.demo.core.error.ErrorCode;

public class InvalidTokenException extends BusinessException{

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
