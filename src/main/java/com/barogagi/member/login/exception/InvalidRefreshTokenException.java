package com.barogagi.member.login.exception;

import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends RuntimeException{

    private final String code;

    public InvalidRefreshTokenException(String code, String message) {
        super(message);
        this.code = code;
    }
}
