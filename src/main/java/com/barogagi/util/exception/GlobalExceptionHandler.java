package com.barogagi.util.exception;

import com.barogagi.config.exception.BusinessException;
import com.barogagi.discord.DiscordNotifier;
import com.barogagi.discord.ErrorThrottle;
import com.barogagi.discord.dto.DiscordErrorMessage;
import com.barogagi.response.ApiResponse;
import com.barogagi.sendMessage.service.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordNotifier discordNotifier;
    private final Environment environment;
    private final CommonService commonService;
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
        if ((commonService.isDev() || commonService.isProd()) && e.getErrorCode().isNotify()) {
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
        if ((commonService.isDev() || commonService.isProd()) && errorThrottle.shouldNotify(key)) {
            discordNotifier.sendError(DiscordErrorMessage.from(e, request, activeProfile(), getStackTrace(e)));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        ErrorCode.INTERNAL_ERROR.getMessage()
                ));
    }

    /**
     * 지원하지 않는 HTTP 메서드
     * - 클라이언트 오류이므로 Discord 알림 제외
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("405 {} {}", request.getMethod(), request.getRequestURI());

        HttpHeaders headers = new HttpHeaders();

        if (e.getSupportedHttpMethods() != null) {
            headers.setAllow(e.getSupportedHttpMethods());
        }

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(ApiResponse.error(
                        ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                        ErrorCode.METHOD_NOT_ALLOWED.getMessage()
                ));
    }

    /**
     * 지원하지 않는 Content-Type (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("415 {} {}", request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(
                        ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                        ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage()
                ));
    }

    /**
     * 잘못된 요청 본문 (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("400 Invalid request body: {} {}", request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_REQUEST_BODY.getCode(),
                        ErrorCode.INVALID_REQUEST_BODY.getMessage()
                ));
    }

    /**
     * 필수 요청 헤더 누락 (400)
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestHeader(MissingRequestHeaderException e, HttpServletRequest request) {
        log.warn("400 Missing request header: {} {}",
                request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.MISSING_REQUEST_HEADER.getCode(),
                        ErrorCode.MISSING_REQUEST_HEADER.getMessage()
                ));
    }

    /**
     * 존재하지 않는 리소스 (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("404 {} {}", request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                        ErrorCode.RESOURCE_NOT_FOUND.getMessage()
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
}

