package com.barogagi.discord.dto;

import com.barogagi.config.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscordErrorMessage {

    private String service;
    private String environment;
    private String uri;
    private String method;
    private String errorCode;
    private String exception;
    private String message;
    private String stackTrace;

    /**
     * BusinessException 전용 (선별 알림)
     */
    public static DiscordErrorMessage from(
            BusinessException e,
            HttpServletRequest request,
            String environment
    ) {
        return DiscordErrorMessage.builder()
                .service("FITPL-API")
                .environment(environment)
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .errorCode(e.getCode())
                .exception(e.getClass().getSimpleName())
                .message(e.getMessage())
                .build();
    }

    /**
     * 예상하지 못한 Exception (장애 알림)
     */
    public static DiscordErrorMessage from(
            Exception e,
            HttpServletRequest request,
            String environment,
            String stackTrace
    ) {
        return DiscordErrorMessage.builder()
                .service("FITPL-API")
                .environment(environment)
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .exception(e.getClass().getSimpleName())
                .message(e.getMessage())
                .stackTrace(stackTrace)
                .build();
    }

    public String format() {
        return """
        🚨 **BACKEND ERROR**
        ───────────────────
        • Service: %s
        • Env: %s
        • URI: %s
        • Method: %s
        • ErrorCode: %s
        • Exception: %s
        • Message: %s

        ```java
        %s
        ```
        """.formatted(
                service,
                environment,
                uri,
                method,
                errorCode == null ? "-" : errorCode,
                exception,
                message,
                stackTrace == null ? "-" : stackTrace
        );
    }
}
