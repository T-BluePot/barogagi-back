package com.barogagi.util.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class BasicException extends BusinessException {

    private final ErrorCode errorCode;

    // 신규 생성자
    public BasicException(ErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
