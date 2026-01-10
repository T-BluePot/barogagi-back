package com.barogagi.mainPage.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MainPageException extends BusinessException {
    public MainPageException(ErrorCode errorCode) {
        super(errorCode);
    }
}
