package com.barogagi.terms.exception;

import lombok.Getter;

@Getter
public class TermsException extends RuntimeException {

    private final String resultCode;

    public TermsException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
