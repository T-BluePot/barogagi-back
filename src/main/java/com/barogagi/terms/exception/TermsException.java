package com.barogagi.terms.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TermsException extends BusinessException {

    public TermsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
