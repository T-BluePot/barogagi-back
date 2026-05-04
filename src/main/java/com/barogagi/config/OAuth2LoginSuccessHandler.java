package com.barogagi.config;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.join.oauth.exception.OAuthException;
import com.barogagi.member.login.dto.LoginResponse;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.redirect.RedirectService;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RedirectService redirectService;

    private final UserMembershipRepository userMembershipRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attrs = token.getPrincipal().getAttributes();

        String userId = extractUserId(attrs);

        LoginResponse login = authService.loginAfterSignup(userId, "web-oauth");

        String nickname = "";
        if(login.tokens().resultCode().equals("R200")) {
            UserMembershipInfo user = userMembershipRepository.findById(login.membershipNo())
                    .orElseThrow(() -> new OAuthException(ErrorCode.NOT_FOUND_USER_INFO));
            nickname = user.getNickName() == null ? "" : user.getNickName();
        }

        // 프론트로 redirect + 데이터 전달
        Map<String, Object> redirectUrlMap = Map.of(
                "resultCode", login.tokens().resultCode(),
                "message", login.tokens().message(),
                "accessToken", login.tokens().accessToken(),
                "accessTokenExpiresIn", login.tokens().accessTokenExpiresIn(),
                "membershipNo", login.membershipNo(),
                "refreshToken", login.tokens().refreshToken(),
                "refreshTokenExpiresIn", login.tokens().refreshTokenExpiresIn(),
                "nickname", nickname
        );

        String redirectUrl = redirectService.successOAuthRedirectUrl(redirectUrlMap);

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

