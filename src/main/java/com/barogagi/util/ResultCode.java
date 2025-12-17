package com.barogagi.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    // API_SECRET_KEY 일치 X
    NOT_EQUAL_API_SECRET_KEY("100", "잘못된 접근입니다."),

    // 서버 오류
    ERROR("400","오류가 발생하였습니다.");

    private final String resultCode;
    private final String message;
}
