package com.barogagi.member.info.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MemberInfoException extends BusinessException {

    public MemberInfoException(ErrorCode errorCode) {
        super(errorCode);
    }
}
