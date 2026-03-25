package com.barogagi.config;

import com.barogagi.member.login.dto.LoginResponse;
import com.barogagi.member.login.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attrs = token.getPrincipal().getAttributes();

        String userId = extractUserId(attrs);

        LoginResponse login = authService.loginAfterSignup(userId, "web-oauth");

        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of(
                "resultCode", login.tokens().resultCode(),
                "message", login.tokens().message(),
                "accessToken", login.tokens().accessToken(),
                "accessTokenExpiresIn", login.tokens().accessTokenExpiresIn(),
                "userId", userId,
                "membershipNo", login.membershipNo(),
                "refreshToken", login.tokens().refreshToken(),
                "refreshTokenExpiresIn", login.tokens().refreshTokenExpiresIn()
        ));
    }

    private String extractUserId(Map<String, Object> attrs) {
        // 1. Google
        Object sub = attrs.get("sub");
        if (sub != null && !sub.toString().isBlank()) {
            return sub.toString();
        }

        // 2. Kakao
        Object id = attrs.get("id");
        if (id != null && !id.toString().isBlank()) {
            return id.toString();
        }

        // 3. Naver (response 내부)
        Object response = attrs.get("response");
        if (response instanceof Map<?, ?> resMap) {
            Object naverId = resMap.get("id");
            if (naverId != null && !naverId.toString().isBlank()) {
                return naverId.toString();
            }
        }

        throw new IllegalArgumentException("OAuth userId를 찾을 수 없습니다.");
    }
}

