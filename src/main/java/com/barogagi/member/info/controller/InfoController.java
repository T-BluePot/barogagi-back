package com.barogagi.member.info.controller;

import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 정보", description = "회원 정보 관련 API")
@RestController
@RequestMapping("/info")
public class InfoController {

    private static final Logger logger = LoggerFactory.getLogger(InfoController.class);


    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회 기능입니다.")
    @GetMapping("/member")
    public ApiResponse selectMemberInfo(Authentication authentication) {
        logger.info("CALL /info/member");
        logger.info("@@ authentication={}", authentication.getPrincipal());

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(authentication == null || !authentication.isAuthenticated()) {
                resultCode = "401";
                message = "인증되지 않은 사용자입니다.";
            } else {

            }

        } catch (Exception e) {
            logger.error("basic login error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }
}
