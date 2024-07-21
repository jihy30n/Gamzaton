package com.example.demo.core.error;

import lombok.Builder;
import lombok.Data;

@Data

public class ErrorEntity {
    private int code;
    private String errorMessage;
    @Builder
    public ErrorEntity(int code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
