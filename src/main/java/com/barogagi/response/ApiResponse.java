package com.barogagi.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {

    // 결과 코드
    private String resultCode;

    // 결과 메시지
    private String message;

    // 데이터
    private T data;
}
