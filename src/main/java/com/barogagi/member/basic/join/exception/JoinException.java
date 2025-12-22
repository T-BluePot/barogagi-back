package com.barogagi.member.basic.join.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class JoinException extends BusinessException {

    public JoinException(String resultCode, String message) {
        super(resultCode, message);
    }
}
