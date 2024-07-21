package com.example.demo.core.error.exeption;


import com.example.demo.core.error.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message, ErrorCode errorCode)
    {
        super(message, errorCode);
    }
}
