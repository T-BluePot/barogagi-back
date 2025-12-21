package com.barogagi.util.exception;

import lombok.Getter;

@Getter
public class BasicException extends RuntimeException {

    private final String resultCode;
    private final ErrorCode errorCode;

    // 신규 생성자
    public BasicException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.resultCode = errorCode.getCode();
    }

    // 기존 생성자
    public BasicException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
        errorCode = null;
    }
}
