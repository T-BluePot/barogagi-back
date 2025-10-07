package com.barogagi.config;

import com.barogagi.member.login.dto.LoginResponse;
import com.barogagi.member.login.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {
        var token = (OAuth2AuthenticationToken) authentication;
        var attrs = token.getPrincipal().getAttributes();

        String extId = String.valueOf(attrs.getOrDefault("sub", attrs.get("id")));
        String userId = extId;

        // successHandler 내부 예시
        LoginResponse login = authService.loginAfterOAuthSignup(userId, "web-oauth");

        // ① refreshToken → HttpOnly 쿠키(프론트 JS에서 안 보임)
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", login.tokens().refreshToken())
                .httpOnly(true).secure(true) // 배포 시 true
                .sameSite("Lax")             // 또는 "Strict"
                .path("/")
                .maxAge(Duration.ofSeconds(login.tokens().refreshTokenExpiresIn()))
                .build();
        res.addHeader("Set-Cookie", refreshCookie.toString());

        // ② accessToken → JSON 바디(프론트가 Authorization 헤더에 사용)
        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of(
                "accessToken", login.tokens().accessToken(),
                "accessTokenExpiresIn", login.tokens().accessTokenExpiresIn(),
                "userId", userId
        ));

    }
}

