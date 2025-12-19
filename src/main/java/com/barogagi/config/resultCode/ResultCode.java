package com.barogagi.config.resultCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    // API_SECRET_KEY 일치 X
    NOT_EQUAL_API_SECRET_KEY("100", "잘못된 접근입니다."),

    // ACCESS TOKEN
    NOT_EXIST_ACCESS_AUTH("401", "접근 권한이 존재하지 않습니다."),
    EXIST_ACCESS_AUTH("200", "회원 번호가 존재합니다."),
    EXPIRE_TOKEN("300", "Token이 만료되었습니다."),

    // 서버 오류
    ERROR("400","오류가 발생하였습니다.");

    private final String resultCode;
    private final String message;
}
