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

    @Operation(summary = "유저 일정 정보 API", description = "메인 화면 - 다가오는 일정 부분에 해당하는 API",
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

    @Operation(summary = "인기 태그 조회 API ", description = "메인 화면 - 오늘 많이 생성되는 일정 부분에 해당하는 API",
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

    @Operation(summary = "인기 지역 조회 API ", description = "메인 화면 - 지금 인기많은 핫 플레이스 부분에 해당하는 API",
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
}
