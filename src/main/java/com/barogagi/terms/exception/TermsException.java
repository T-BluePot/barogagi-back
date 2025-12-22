package com.barogagi.terms.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class TermsException extends BusinessException {

    public TermsException(String resultCode, String message) {
        super(resultCode, message);
    }
}
