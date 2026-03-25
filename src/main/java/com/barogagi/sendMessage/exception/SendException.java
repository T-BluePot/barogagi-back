package com.barogagi.sendMessage.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class SendException extends BusinessException {

    public SendException(ErrorCode errorCode) {
        super(errorCode);
    }
}
