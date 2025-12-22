package com.barogagi.approval.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class ApprovalException extends BusinessException {

    public ApprovalException(String resultCode, String message) {
        super(resultCode, message);
    }
}
