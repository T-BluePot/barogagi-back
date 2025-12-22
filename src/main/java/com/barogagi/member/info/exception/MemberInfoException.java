package com.barogagi.member.info.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class MemberInfoException extends BusinessException {

    public MemberInfoException(String resultCode, String message) {
        super(resultCode, message);
    }
}
