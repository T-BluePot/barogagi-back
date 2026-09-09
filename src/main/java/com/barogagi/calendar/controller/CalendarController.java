package com.barogagi.calendar.controller;

import com.barogagi.calendar.service.CalendarService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "달력", description = "달력에 필요한 API")
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "공휴일 정보 조회 기능", description = "공휴일 정보 조회 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "잘못된 접근입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-400", description = "잘못된 요청입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "H201", description = "공휴일 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "H200", description = "공휴일 정보 조회 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C401", description = "데이터 형식이 올바르지 않습니다.")
            })
    @GetMapping("/holidays")
    public ApiResponse getHolidays(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam("year") String year, @RequestParam("month") String month) {
        return calendarService.getHolidays(apiSecretKey, year, month);
    }
}
