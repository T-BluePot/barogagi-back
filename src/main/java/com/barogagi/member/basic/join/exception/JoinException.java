package com.barogagi.member.basic.join.exception;

import lombok.Getter;

@Getter
public class JoinException extends RuntimeException {

    private final String resultCode;

    public JoinException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
