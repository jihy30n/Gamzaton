package com.example.demo.core.error.exeption;

import com.example.demo.core.error.ErrorCode;
import lombok.Getter;

@Getter
public class UnAuthorizedException extends BusinessException {

    public UnAuthorizedException(String message, ErrorCode errorCode) {
        super(message,errorCode);
    }
}
