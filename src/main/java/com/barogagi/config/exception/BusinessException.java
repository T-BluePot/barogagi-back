package com.barogagi.config.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String resultCode;

    public BusinessException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
