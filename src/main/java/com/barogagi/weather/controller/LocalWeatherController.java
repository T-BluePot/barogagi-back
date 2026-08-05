package com.barogagi.weather.controller;

import com.barogagi.response.ApiResponse;
import com.barogagi.weather.service.LocalWeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "지역 날씨 정보", description = "특정 지역 날씨 정보 관련 API")
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class LocalWeatherController {

    private final LocalWeatherService localWeatherService;

    @Operation(summary = "단기 예보 날씨 조회 기능",
            description =
                    "단기 예보 날씨 조회 기능입니다. \r\n\r\n" +

                            "오늘 포함 3일간의 날씨가 조회됩니다. " +
                            "(예: 2026.07.27 기준 2026.07.27 ~ 2026.07.29의 날씨 조회 가능) \r\n\r\n" +

                            "areaCd : 지역코드, " +
                            "sigunguCd : 시군구코드 " +
                            "(단, 둘 중 하나의 코드라도 빈 값일 경우 " +
                            "기본값인 서울특별시 종로구 기준 날씨 제공) \r\n\r\n" +

                            "startDate : 조회하고 싶은 시작 날짜를 입력합니다. \r\n\r\n" +
                            "endDate : 조회하고 싶은 종료 날짜를 입력합니다.\r\n\r\n" +
                            "* 형식 : yyyyMMdd \r\n\r\n" +
                            "* startDate, endDate가 동일할 경우 해당 날짜(특정 날짜)의 날씨가 조회됩니다. \r\n\r\n" +
                            "* startDate, endDate가 다를 경우 해당 날짜(기간)의 날씨가 조회됩니다. \r\n\r\n" +
                            "* startDate, endDate가 ALL일 경우 오늘 포함 3일간의 날씨가 조회됩니다."
            ,
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "잘못된 접근입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW401", description = "올바른 형식이 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C400", description = "지역 코드 정보를 찾을 수 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW404", description = "날씨를 조회할 수 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW402", description = "조회 기간이 초과되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW200", description = "날씨 조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-400", description = "잘못된 요청입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다.")
            })
    @GetMapping("/short")
    public ApiResponse getShortWeather(@RequestHeader("API-KEY") String apiSecretKey,
                                       @RequestParam(name = "areaCd", required = false) String areaCd,
                                       @RequestParam(name = "sigunguCd", required = false) String sigunguCd,
                                       @RequestParam(name = "startDate", defaultValue = "ALL") String startDate,
                                       @RequestParam(name = "endDate", defaultValue = "ALL") String endDate) {

        return localWeatherService.getShortWeather(apiSecretKey, areaCd, sigunguCd, startDate, endDate);
    }

    @Operation(summary = "중기 예보 날씨 조회 기능",
            description = "중기 예보 날씨 조회 기능입니다. \r\n\r\n" +

                    "오늘 이후 4-10일간의 날씨가 조회됩니다. " +
                    "(예: 2026.07.31 기준 2026.08.04 ~ 2026.08.10의 날씨 조회 가능. 3일 데이터는 조회 불가능. 그리고 4일 데이터는 오후부터 조회 불가능) \r\n\r\n" +

                    "areaCd : 지역코드, " +
                    "sigunguCd : 시군구코드 " +
                    "(단, 둘 중 하나의 코드라도 빈 값일 경우 " +
                    "기본값인 서울특별시 종로구 기준 날씨 제공) \r\n\r\n" +

                    "startDate : 조회하고 싶은 시작 날짜를 입력합니다. \r\n\r\n" +
                    "endDate : 조회하고 싶은 종료 날짜를 입력합니다.\r\n\r\n" +
                    "* 형식 : yyyyMMdd \r\n\r\n" +
                    "* startDate, endDate가 동일할 경우 해당 날짜(특정 날짜)의 날씨가 조회됩니다. \r\n\r\n" +
                    "* startDate, endDate가 다를 경우 해당 날짜(기간)의 날씨가 조회됩니다. \r\n\r\n" +
                    "* startDate, endDate가 ALL일 경우 오늘 이후 4-10일간의 날씨가 조회됩니다."
            ,
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "잘못된 접근입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW401", description = "올바른 형식이 아닙니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C400", description = "지역 코드 정보를 찾을 수 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW404", description = "날씨를 조회할 수 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW402", description = "조회 기간이 초과되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "LW200", description = "날씨 조회 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-400", description = "잘못된 요청입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다.")
            })
    @GetMapping("/mid")
    public ApiResponse getMidWeather(@RequestHeader("API-KEY") String apiSecretKey,
                                       @RequestParam(name = "areaCd", required = false) String areaCd,
                                       @RequestParam(name = "sigunguCd", required = false) String sigunguCd,
                                       @RequestParam(name = "startDate", defaultValue = "ALL") String startDate,
                                       @RequestParam(name = "endDate", defaultValue = "ALL") String endDate) {

        return localWeatherService.getMidWeather(apiSecretKey, areaCd, sigunguCd, startDate, endDate);
    }
}
