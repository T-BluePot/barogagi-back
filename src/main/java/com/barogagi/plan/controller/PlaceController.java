package com.barogagi.plan.controller;

import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.plan.query.service.PlaceQueryService;
import com.barogagi.response.ApiResponse;
import com.barogagi.schedule.controller.ScheduleController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장소", description = "장소 관련 API")
@RestController
@RequestMapping("/api/v1/place")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class PlaceController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final PlaceQueryService placeQueryService;
    private final String API_SECRET_KEY;

    public PlaceController(Environment environment,
                           PlaceQueryService placeQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.placeQueryService = placeQueryService;
    }

    @Operation(summary = "장소 검색 기능",
            description = "사용자가 찾고 싶은 장소를 Kakao API로 검색하는 기능입니다.<br>" +
                    "- 일정을 생성하는 API를 호출할 때, 이 API에서 받은 placeName, placeUrl, addressName을 보내주세요.<br>" +
                    "- regionNum은 쓰지 않는 필드입니다. (null 값이 전달됨)")
    @GetMapping("/keyword-search")
    public ApiResponse searchPlace(@Parameter(description = "검색 키워드", example = "스타벅스")
                                      @RequestParam String searchKeyword, HttpServletRequest request) {
        logger.info("CALL /place/keyword-search");
        return placeQueryService.searchPlace(searchKeyword, request);
    }

    @Operation(
            summary = "30일 내 인기 장소 조회",
            description = "최근 30일간 PLAN에 가장 많이 저장된 장소 링크 기준으로 인기 장소를 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "P200",
                            description = "인기 장소 검색에 성공했습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "P102",
                            description = "인기 장소 검색 결과를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "COMMON-500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    @GetMapping("/popular")
    public ApiResponse getPopularPlaces(
            @Parameter(description = "반환할 인기 장소 개수", example = "5")
            @RequestParam(defaultValue = "5") Integer limit,
            HttpServletRequest request) {
        logger.info("CALL /api/v1/place/popular");
        logger.info("[input] limit={}", limit);

        return placeQueryService.searchPopularPlaces(limit, request);
    }
}
