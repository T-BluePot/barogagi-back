package com.barogagi.member.join.oauth.exception;

import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class OAuthJoinException extends AuthenticationException {

    private final ErrorCode errorCode;

    public OAuthJoinException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
