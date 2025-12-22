package com.barogagi.util.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class BasicException extends BusinessException {

    private final String resultCode;
    private final ErrorCode errorCode;

    // 신규 생성자
    public BasicException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
        this.errorCode = errorCode;
        this.resultCode = errorCode.getCode();
    }

    // 기존 생성자
    public BasicException(String resultCode, String message) {
        super(resultCode, message);
        this.resultCode = resultCode;
        errorCode = null;
    }
}
