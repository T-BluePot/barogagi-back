package com.barogagi.util.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class BasicException extends BusinessException {

    public BasicException(String resultCode, String message) {
        super(resultCode, message);
    }
}
