package com.barogagi.member.oauth.join.controller;

import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "oAuth 회원가입", description = "OAuth 회원가입 관련 API")
@RestController
@RequestMapping("/membership/oauth/join")
public class OAuthJoinController {
    private static final Logger logger = LoggerFactory.getLogger(OAuthJoinController.class);

    private final String API_SECRET_KEY;

    public OAuthJoinController(Environment environment) {
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
    }

    @Operation(summary = "OAuth 회원가입 기능", description = "OAuth 회원가입 기능입니다.")
    @PostMapping("/oauth/membership/join")
    public ApiResponse oauthJoin() {
        logger.info("CALL /membership/oauth/join/oauth/membership/join");
        logger.info("[input] API_SECRET_KEY={}", API_SECRET_KEY);

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

        } catch (Exception e) {
            logger.error("OAuth 회원가입 중 오류 발생: {}", e.getMessage());
            resultCode = "ERROR";
            message = "OAuth 회원가입 중 오류가 발생했습니다.";
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return apiResponse;
    }
}
