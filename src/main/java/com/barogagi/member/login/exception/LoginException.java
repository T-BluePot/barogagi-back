package com.barogagi.member.login.exception;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {
    private final String code;

    public LoginException(String code, String message) {
        super(message);
        this.code = code;
    }
}
