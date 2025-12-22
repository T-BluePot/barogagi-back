package com.barogagi.member.login.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException(String resultCode, String message) {
        super(resultCode, message);
    }
}
