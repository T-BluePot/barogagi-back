package com.barogagi.region.controller;

import com.barogagi.region.query.service.RegionGeoCodeService;
import com.barogagi.region.query.service.RegionQueryService;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "지역", description = "지역 관련 API")
@RestController
@RequestMapping("/api/v1/region")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "apiKeyAuth")
public class RegionController {
    private static final Logger logger = LoggerFactory.getLogger(RegionController.class);

    private final InputValidate inputValidate;
    private final RegionGeoCodeService regionGeoCodeService;
    private final RegionQueryService regionQueryService;
    private final String API_SECRET_KEY;

    public RegionController(Environment environment,
                            InputValidate inputValidate,
                            RegionGeoCodeService regionGeoCodeService,
                            RegionQueryService regionQueryService) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
        this.inputValidate = inputValidate;
        this.regionGeoCodeService = regionGeoCodeService;
        this.regionQueryService = regionQueryService;
    }

    @Operation(
            summary = "주소를 x,y 좌표로 변환하는 기능",
            description = "주소 번호를 받아서 x,y 좌표로 변환하는 기능입니다",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "잘못된 접근입니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "C400",
                            description = "잘못된 요청입니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "R201",
                            description = "지역 정보를 찾을 수 없습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "주소 좌표 변환 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "C500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    @GetMapping("/geocode")
    public ApiResponse getGeocode(
            @Parameter(description = "법정동/행정동의 주소 번호", example = "1")
            @RequestParam Integer regionNum,
            HttpServletRequest request) {
        logger.info("CALL /api/v1/region/geocode");
        logger.info("[input] regionNum={}", regionNum);

        return regionGeoCodeService.getGeocode(regionNum, request);
    }

    @Operation(
            summary = "주소 목록을 검색하는 기능",
            description = "주소 목록을 검색하는 기능입니다.<br>" +
                    "REGION table에 저장된 값 중, level 1부터 4까지 가장 정확도 높은 순으로 지역명 최대 10개를 리턴합니다.<br>" +
                    "행정구역 단계(시/도, 시/군/구, 동/면/리)를 조합하여 결과를 반환하며, 중복되는 상위 주소(예: '서울특별시 강남구')는 한 번만 표시됩니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "A100",
                            description = "잘못된 접근입니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "주소 목록 검색 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "C500",
                            description = "서버 오류가 발생했습니다."
                    )
            }
    )
    @GetMapping("/search-list")
    public ApiResponse searchList(
            @Parameter(description = "검색할 주소명", example = "강남")
            @RequestParam String regionQuery,
            HttpServletRequest request) {
        logger.info("CALL /api/v1/region/search-list");

        return regionQueryService.searchList(regionQuery, request);
    }
}