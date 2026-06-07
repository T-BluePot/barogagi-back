package com.barogagi.redirect;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedirectService {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final Environment environment;

    public String serverUrl() {
        // 서버 종류
        String[] profiles = environment.getActiveProfiles();
        String serverType = (profiles.length > 0) ? profiles[0] : "";

        // 서버별 주소
        List<String> addresses = Arrays.asList(allowedOrigins.split(","));

        String url = "";
        if(serverType.equals("dev")) {  // 테스트 서버
            url = addresses.get(1);
        } else if(serverType.equals("prod")) {  // 실서버
            url = "barogagiapp:/";
        } else {  // 로컬 서버
            url = addresses.get(0);
        }

        return url;
    }

    // OAuth 후 성공 시 프론트로 redirect
    public String successOAuthRedirectUrl(Map<String, Object> paramMap) {

        String resultCode = String.valueOf(paramMap.get("resultCode") == null ? "" : paramMap.get("resultCode"));
        String message = String.valueOf(paramMap.get("message") == null ? "" : paramMap.get("message"));
        String accessToken = String.valueOf(paramMap.get("accessToken") == null ? "" : paramMap.get("accessToken"));
        String accessTokenExpiresIn = String.valueOf(paramMap.get("accessTokenExpiresIn") == null ? "" : paramMap.get("accessTokenExpiresIn"));
        String membershipNo = String.valueOf(paramMap.get("membershipNo") == null ? "" : paramMap.get("membershipNo"));
        String refreshToken = String.valueOf(paramMap.get("refreshToken") == null ? "" : paramMap.get("refreshToken"));
        String refreshTokenExpiresIn = String.valueOf(paramMap.get("refreshTokenExpiresIn") == null ? "" : paramMap.get("refreshTokenExpiresIn"));

        String nickname = String.valueOf(paramMap.get("nickname"));
        String nicknameYn = nickname.isEmpty() ? "N" : "Y";

        return serverUrl() + "/auth/oauth/callback" +
                "?resultCode=" + resultCode +
                "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                "&accessToken=" + accessToken +
                "&accessTokenExpiresIn=" + accessTokenExpiresIn +
                "&membershipNo=" + membershipNo +
                "&refreshToken=" + refreshToken +
                "&refreshTokenExpiresIn=" + refreshTokenExpiresIn +
                "&nicknameYn=" + nicknameYn +
                "&nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);
    }

    // OAuth 후 실패 시 프론트로 redirect
    public String failOAuthRedirectUrl(Map<String, Object> paramMap) {

        String resultCode = String.valueOf(paramMap.get("resultCode") == null ? "" : paramMap.get("resultCode"));
        String message = String.valueOf(paramMap.get("message") == null ? "" : paramMap.get("message"));

        return serverUrl() + "/auth/oauth/callback" +
                "?resultCode=" + resultCode +
                "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
