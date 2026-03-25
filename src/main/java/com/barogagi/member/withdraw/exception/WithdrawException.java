package com.barogagi.member.withdraw.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class WithdrawException extends BusinessException {

    public WithdrawException(ErrorCode errorCode) {
        super(errorCode);
    }
}
