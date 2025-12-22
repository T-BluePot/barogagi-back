package com.barogagi.member.login.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class LoginException extends BusinessException {

    public LoginException(String resultCode, String message) {
        super(resultCode, message);
    }
}
