package com.barogagi.push.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;

public class PushException extends BusinessException {
    public PushException(ErrorCode errorCode) {
        super(errorCode);
    }
}
