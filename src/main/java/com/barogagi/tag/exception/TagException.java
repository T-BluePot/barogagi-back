package com.barogagi.tag.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TagException extends BusinessException {

    public TagException(ErrorCode errorCode) {
        super(errorCode);
    }
}
