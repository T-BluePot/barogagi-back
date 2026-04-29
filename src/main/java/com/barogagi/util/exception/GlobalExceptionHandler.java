package com.barogagi.util.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.discord.DiscordNotifier;
import com.barogagi.discord.ErrorThrottle;
import com.barogagi.discord.dto.DiscordErrorMessage;
import com.barogagi.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier discordNotifier;
    private final Environment environment;
    private final ErrorThrottle errorThrottle;

    /**
     * 비즈니스 예외 (BasicException 포함)
     * - 의도된 흐름
     * - ErrorCode.notify = true인 경우에만 Discord 알림 O
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        if ((isDev() || isProd()) && e.getErrorCode().isNotify()) {
            discordNotifier.sendError(DiscordErrorMessage.from(e, request, activeProfile()));
        }

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.result(e.getCode(), e.getMessage()));
    }

    /**
     * Validation / Client 오류
     * - Discord 알림 X
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ApiResponse<?>> handleValidationException(Exception e) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_REQUEST.getCode(),
                        ErrorCode.INVALID_REQUEST.getMessage()
                ));
    }

    /**
     * 🔥 예상하지 못한 서버 장애
     * - Discord 알림 O
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnhandledException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", e);

        String key = e.getClass().getName() + request.getRequestURI();

        // Discord 알림 전송
        if ((isDev() || isProd()) && errorThrottle.shouldNotify(key)) {
            discordNotifier.sendError(DiscordErrorMessage.from(e, request, activeProfile(), getStackTrace(e)));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        ErrorCode.INTERNAL_ERROR.getMessage()
                ));
    }

    private String activeProfile() {
        String[] profiles = environment.getActiveProfiles();
        return (profiles.length > 0) ? profiles[0] : "";
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        // Discord 메시지 길이 제한 대응
        return sw.toString().length() > 1500
                ? sw.toString().substring(0, 1500)
                : sw.toString();
    }

    private boolean isProd() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private boolean isDev() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }
}

