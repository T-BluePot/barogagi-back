package com.barogagi.response;

import com.barogagi.util.exception.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiResponse<T> {

    // 결과 코드
    private String code;

    // 결과 메시지
    private String message;

    // 데이터
    private T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = "SUCCESS";
        res.message = message;
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = code;
        res.message = message;
        return res;
    }

    public static <T> ApiResponse<T> resultData(T data, String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = code;
        res.message = message;
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> result(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = code;
        res.message = message;
        return res;
    }

    public static <T> ApiResponse<T> result(ErrorCode errorCode) {
        ApiResponse<T> res = new ApiResponse<>();
        res.code = errorCode.getCode();
        res.message = errorCode.getMessage();
        return res;
    }
}
