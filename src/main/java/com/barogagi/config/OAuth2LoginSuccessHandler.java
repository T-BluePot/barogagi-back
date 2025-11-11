package com.barogagi.config;

import com.barogagi.member.basic.login.dto.LoginVO;
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

        res.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of(
                "accessToken", login.tokens().accessToken(),
                "accessTokenExpiresIn", login.tokens().accessTokenExpiresIn(),
                "userId", userId,
                "membershipNo", login.membershipNo(),
                "refreshToken", login.tokens().refreshToken(),
                "refreshTokenExpiresIn", login.tokens().refreshTokenExpiresIn()
        ));

    }
}

