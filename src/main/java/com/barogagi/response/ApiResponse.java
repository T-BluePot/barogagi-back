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

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.resultCode = "200";
        res.message = message;
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.resultCode = code;
        res.message = message;
        return res;
    }

    public static <T> ApiResponse<T> resultData(T data, String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.resultCode = code;
        res.message = message;
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> result(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.resultCode = code;
        res.message = message;
        return res;
    }
}
