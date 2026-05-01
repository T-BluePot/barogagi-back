package com.barogagi.config;

import com.barogagi.member.login.dto.LoginResponse;
import com.barogagi.member.login.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final Environment environment;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attrs = token.getPrincipal().getAttributes();

        String userId = extractUserId(attrs);

        LoginResponse login = authService.loginAfterSignup(userId, "web-oauth");

        // 서버 종류
        String[] profiles = environment.getActiveProfiles();
        String serverType = (profiles.length > 0) ? profiles[0] : "";

        // 서버별 주소
        List<String> addresses = Arrays.asList(allowedOrigins.split(","));

        String url = "";
        if(serverType.equals("dev")) {  // 테스트 서버
            url = addresses.get(1);
        } else if(serverType.equals("prod")) {  // 실서버
            url = addresses.get(2);
        } else {  // 로컬 서버
            url = addresses.get(0);
        }

        // 프론트로 redirect + 데이터 전달
        String redirectUrl = url + "/oauth/success" +
                "?resultCode=" + login.tokens().resultCode() +
                "&message=" + URLEncoder.encode(login.tokens().message(), StandardCharsets.UTF_8) +
                "&accessToken=" + login.tokens().accessToken() +
                "&accessTokenExpiresIn=" + login.tokens().accessTokenExpiresIn() +
                "&userId=" + userId +
                "&membershipNo=" + login.membershipNo() +
                "&refreshToken=" + login.tokens().refreshToken() +
                "&refreshTokenExpiresIn=" + login.tokens().refreshTokenExpiresIn();

        res.sendRedirect(redirectUrl);
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

