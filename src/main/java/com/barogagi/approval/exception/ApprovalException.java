package com.barogagi.approval.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApprovalException extends BusinessException {

    public ApprovalException(ErrorCode errorCode) {
        super(errorCode);
    }
}
