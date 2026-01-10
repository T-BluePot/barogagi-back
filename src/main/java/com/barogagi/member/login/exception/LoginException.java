package com.barogagi.member.login.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class LoginException extends BusinessException {

    public LoginException(ErrorCode errorCode) {
        super(errorCode);
    }
}
