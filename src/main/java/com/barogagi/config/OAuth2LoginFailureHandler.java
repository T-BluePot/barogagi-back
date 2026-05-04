package com.barogagi.config;

import com.barogagi.member.join.oauth.exception.OAuthJoinException;
import com.barogagi.redirect.RedirectService;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final RedirectService redirectService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // 기본값
        String resultCode = ErrorCode.FAIL_OAUTH2_LOGIN.getCode();
        String message = ErrorCode.FAIL_OAUTH2_LOGIN.getMessage();

        if (exception instanceof OAuthJoinException oAuthJoinException) {
            ErrorCode errorCode = oAuthJoinException.getErrorCode();
            resultCode = errorCode.getCode();
            message = errorCode.getMessage();
        }

        Map<String, Object> redirectUrlMap = Map.of(
                "resultCode", resultCode,
                "message", message
        );

        // 프론트로 redirect + 데이터 전달
        String redirectUrl = redirectService.failOAuthRedirectUrl(redirectUrlMap);

        response.sendRedirect(redirectUrl);
    }
}
