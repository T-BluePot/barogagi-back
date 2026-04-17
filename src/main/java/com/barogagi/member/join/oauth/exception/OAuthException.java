package com.barogagi.member.join.oauth.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class OAuthException extends BusinessException {
    public OAuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
