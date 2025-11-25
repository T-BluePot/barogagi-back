package com.barogagi.mainPage.exception;

import lombok.Getter;

@Getter
public class MainPageException extends RuntimeException {

    private final String resultCode;

    public MainPageException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
