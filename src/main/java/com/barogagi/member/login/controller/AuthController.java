package com.barogagi.member.login.controller;

import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.service.AccountService;
import com.barogagi.member.login.service.AuthService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "TOKEN 재발급, 로그아웃, 탈퇴", description = "TOKEN 재발급, 로그아웃, 탈퇴 관련 API")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AccountService accountService;

    public AuthController(AuthService authService,
                          AccountService accountService) {
        this.authService = authService;
        this.accountService = accountService;
    }

    @Operation(summary = "토큰 재발급", description = "Access 토큰 만료 시, Refresh 토큰으로 Access 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {

        logger.info("CALL /auth/refresh");

        try {
            String rt = Optional.ofNullable(refreshHeader)
                    .or(() -> Optional.ofNullable(body == null ? null : body.get("refreshToken")))
                    .orElse(null);

            if (rt == null || rt.isBlank()) {
                return ResponseEntity.status(401).body(Map.of("error", "refresh_required"));
            }

            TokenPair pair = authService.rotate(rt); // ❗️핵심 로직 (아래 2) 참조)

            return ResponseEntity.ok(Map.of(
                    "accessToken", pair.accessToken(),
                    "accessTokenExpiresIn", pair.accessTokenExpiresIn(),
                    "refreshToken", pair.refreshToken(),
                    "refreshTokenExpiresIn", pair.refreshTokenExpiresIn()
            ));

        } catch (InvalidRefreshTokenException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                .body(Map.of(
                        "resultCode", "400",
                        "errorCode", e.getCode(),
                        "message", e.getMessage(),
                        "needLogin", true
                ));
        }
    }

    /**
     * 현재 기기 로그아웃: 전달된 refreshToken이 속한 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE
     * 입력 경로:
     *  - 헤더: Refresh-Token: <refresh>
     *  - 바디: { "refreshToken": "<refresh>" }
     */
    @Operation(summary = "현재 기기 로그아웃", description = "현재 기기 로그아웃: 전달된 refreshToken이 속한 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String refresh = refreshHeader;
        if ((refresh == null || refresh.isBlank()) && body != null) {
            refresh = body.get("refreshToken");
        }
        if (refresh != null && !refresh.isBlank()) {
            authService.logout(refresh); // DB REVOKE
        }
        return ResponseEntity.ok(Map.of("resultCode", "200", "message", "로그아웃 되었습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 API",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "refresh token이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "회원 탈퇴 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/member/delete")
    public ApiResponse deleteMe(@RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
                                @RequestBody(required = false) Map<String, String> body) {

        logger.info("CALL /auth/member/delete");

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            String refreshToken = Optional.ofNullable(refreshHeader)
                    .or(() -> Optional.ofNullable(body == null ? null : body.get("refreshToken")))
                    .orElse(null);

            if (refreshToken == null || refreshToken.isBlank()) {
                throw new InvalidRefreshTokenException("100", "refresh token이 존재하지 않습니다.");
            }

            // refresh token을 이용해서 membershipNo 구하기

            Map<String, String> resultMap = authService.selectUserInfoByToken(refreshToken);
            if(!resultMap.get("resultCode").equals("200")) {
                throw new InvalidRefreshTokenException(resultMap.get("resultCode"), resultMap.get("message"));
            }

            String membershipNo = resultMap.get("membershipNo");

            int deleteResult = accountService.deleteMyAccount(membershipNo);
            if(deleteResult > 0) {
                resultCode = "200";
                message = "회원 탈퇴되었습니다.";
            } else {
                resultCode = "300";
                message = "회원 탈퇴 실패하였습니다.";
            }

        } catch (InvalidRefreshTokenException ex) {
            resultCode = ex.getCode();
            message = ex.getMessage();

        } catch (Exception e) {
            logger.error("error", e);
            resultCode = "400";
            message = "오류가 발생하였습니다.";

        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }

        return apiResponse;
    }
}


