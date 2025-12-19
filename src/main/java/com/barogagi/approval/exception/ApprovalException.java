package com.barogagi.approval.exception;

import lombok.Getter;

@Getter
public class ApprovalException extends RuntimeException {

    private final String resultCode;

    public ApprovalException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
