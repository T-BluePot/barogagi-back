package com.barogagi.push.controller;

import com.barogagi.push.dto.PushTokenRequest;
import com.barogagi.push.service.PushTokenService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM Token", description = "FCM Token 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @Operation(summary = "FCM Token 저장 기능", description = "FCM Token 저장 기능입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "T200", description = "FCM TOKEN 저장이 완료되었습니다"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-400", description = "잘못된 요청입니다."),
            })
    @PostMapping("/token")
    public ApiResponse saveToken(HttpServletRequest request, @RequestBody PushTokenRequest pushTokenRequest) {
        return pushTokenService.saveToken(request, pushTokenRequest);
    }

    @Operation(summary = "FCM Token 삭제 기능", description = "FCM Token 삭제 기능입니다. \r\n\r\n" +
            "deviceType, fcmToken 값이 모두 없을 경우 해당 회원의 모든 fcm Token이 삭제됩니다.\r\n\r\n" +
            "deviceType, fcmToken 값이 모두 있을 경우 해당 회원의 deviceType에 매핑된 fcm Token이 삭제됩니다. \r\n\r\n" +
            "두 개의 값 중 하나만 있을 경우 해당 회원의 모든 fcm Token이 삭제됩니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A401", description = "접근 권한이 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "L102", description = "회원 정보가 존재하지 않습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "TD200", description = "FCM TOKEN 삭제가 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-400", description = "잘못된 요청입니다."),
            })
    @DeleteMapping("/token")
    public ApiResponse delToken(HttpServletRequest request,
                                @RequestParam(name = "fcmToken", required = false) String fcmToken,
                                @RequestParam(name = "deviceType", required = false) String deviceType) {
        return pushTokenService.delToken(request, fcmToken, deviceType);
    }
}
