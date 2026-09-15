package com.barogagi.calendar.service;

import com.barogagi.calendar.dto.HolidaysItem;
import com.barogagi.calendar.dto.HolidaysResponseDTO;
import com.barogagi.calendar.exception.CalendarException;
import com.barogagi.config.ApiClient;
import com.barogagi.mainPage.exception.MainPageException;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final ApiClient apiClient;
    private final Validator validator;
    private final InputValidate inputValidate;

    public ApiResponse getHolidays(String apiSecretKey, String year, String month) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new MainPageException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 데이터 검증
        if(inputValidate.isEmpty(year) || inputValidate.isEmpty(month)){
            throw new CalendarException(ErrorCode.EMPTY_DATA);
        }

        if (Integer.valueOf(year) < 1000 || Integer.valueOf(year) > 9999
                || Integer.valueOf(month) < 1 || Integer.valueOf(month) > 12) {
            return ApiResponse.result(ErrorCode.INVALID_DATA);
        }

        HolidaysResponseDTO response = apiClient.getRestDeInfo(year, month);

        // resultCode = 00 일 경우 조회 성공
        String resultCode = response.getResponse().getHeader().getResultCode();
        String message = response.getResponse().getHeader().getResultMsg();
        log.info("Holidays ResultCode={}, Message={}", resultCode, message);

        if (resultCode.equals("00")) {
            if (response.getResponse().getBody().getItems() != null && response.getResponse().getBody().getItems().getItem() != null) {
                List<HolidaysItem> data = response.getResponse().getBody().getItems().getItem();
                return ApiResponse.resultData(data, "H200", "공휴일 정보 조회 성공하였습니다.");
            } else {
                return ApiResponse.resultData(null, "H201", "공휴일 정보가 존재하지 않습니다.");
            }

        } else {
            return ApiResponse.result(ErrorCode.NOT_FOUND_HOLIDAYS);
        }
    }
}
