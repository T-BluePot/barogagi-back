package com.barogagi.mainPage.exception;

import com.barogagi.config.exception.BusinessException;
import lombok.Getter;

@Getter
public class MainPageException extends BusinessException {

    public MainPageException(String resultCode, String message) {
        super(resultCode, message);
    }
}
