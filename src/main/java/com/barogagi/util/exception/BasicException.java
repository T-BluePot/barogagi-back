package com.barogagi.util.exception;

import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {

    private final String resultCode;

    public BasicException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
