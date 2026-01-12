package com.barogagi.member.join.basic.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class JoinException extends BusinessException {

    public JoinException(ErrorCode errorCode) {
        super(errorCode);
    }
}
