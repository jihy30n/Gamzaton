package com.example.demo.core.error.exeption;

import com.example.demo.core.error.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends BusinessException{

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
