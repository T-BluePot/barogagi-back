package com.barogagi.terms.controller;

import com.barogagi.response.ApiResponse;
import com.barogagi.terms.dto.*;
import com.barogagi.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "약관", description = "약관 관련 API")
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(summary = "약관 목록 조회 기능", description = "약관 목록 조회 기능입니다. <br> 회원가입 시 사용할 경우 termsType 값을 JOIN-MEMBERSHIP 값으로 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T200", description = "약관 조회에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T102", description = "약관이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @GetMapping
    public ApiResponse termsList(@RequestHeader("API-KEY") String apiSecretKey, @RequestParam String termsType){
        return termsService.termsListProcess(apiSecretKey, termsType);
    }

    @Operation(summary = "약관 동의 여부 저장 기능", description = "약관 동의 여부 저장 기능입니다. agreeYn은 Y 또는 N을 보내주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T200", description = "약관 저장에 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L102", description = "해당 사용자의 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T300", description = "약관 저장에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T400", description = "필수 약관 항목에 동의해주셔야 서비스 이용이 가능합니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T401", description = "유효한 약관이 아닙니다.")
            })
    @PostMapping("/terms-agreements")
    public ApiResponse insertTermsAgree(HttpServletRequest request, @RequestBody TermsDTO termsDTO) {
        return termsService.termsAgreementsProcess(request, termsDTO);
    }
}
