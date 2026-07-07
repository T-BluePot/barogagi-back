package com.barogagi.mainPage.controller;

import com.barogagi.mainPage.response.MainPageResponse;
import com.barogagi.mainPage.service.MainPageService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "메인 화면", description = "메인 화면에 필요한 API")
@RestController
@RequestMapping("/api/v1/home")
public class MainPageController {

    private final MainPageService mainPageService;

    @Autowired
    public MainPageController(MainPageService mainPageService) {
        this.mainPageService = mainPageService;
    }

    @Operation(summary = "유저 일정 정보 기능", description = "메인 화면 - 다가오는 일정 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M201", description = "일정이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M200", description = "조회 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/me/schedules")
    public MainPageResponse selectUserScheduleInfo(HttpServletRequest request) {
        return mainPageService.selectUserScheduleInfoProcess(request);
    }

    @Operation(summary = "인기 태그 조회 기능", description = "메인 화면 - 오늘 많이 생성되는 일정 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M200", description = "인기 태그 조회 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M201", description = "인기 태그 목록이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/tags/popular")
    public ApiResponse selectPopularTagList(@RequestHeader("API-KEY") String apiSecretKey) {
        return mainPageService.selectPopularTagList(apiSecretKey);
    }

    @Operation(summary = "인기 지역 조회 기능 ", description = "메인 화면 - 지금 인기 많은 핫 플레이스 부분에 해당하는 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M200", description = "인기 지역 조회 완료하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "M201", description = "인기 지역 목록이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/regions/popular")
    public ApiResponse selectPopularRegionList(@RequestHeader("API-KEY") String apiSecretKey) {
        return mainPageService.selectPopularRegionList(apiSecretKey);
    }

    @Operation(summary = "공공기관 지역코드 조회 기능", description = "공공기관 지역코드 조회 API(오늘의 핫플레이스 조회 시 필요한 코드 전달)",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "잘못된 접근입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C400", description = "지역 코드 정보를 찾을 수 없습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C200", description = "지역 코드 정보를 조회하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/regions/code")
    public ApiResponse selectHotPlaceList(@RequestHeader("API-KEY") String apiSecretKey,
                                          @RequestParam("type") String type) {

        return mainPageService.selectKorTourOrgLocalCode(apiSecretKey, type);
    }

    @Operation(summary = "핫플레이스 조회 기능", description = "메인 화면 - 오늘의 핫플레이스 부분에 해당하는 API(한국관광공사 데이터 기반)",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping("/regions/hot-place")
    public ApiResponse selectHotPlaceList(@RequestHeader("API-KEY") String apiSecretKey,
                                          @RequestParam("areaCd") String areaCd,
                                          @RequestParam("sigunguCd") String sigunguCd) {
        return mainPageService.selectHotPlaceList(apiSecretKey, areaCd, sigunguCd);
    }
}
