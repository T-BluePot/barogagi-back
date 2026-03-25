package com.barogagi.member.withdraw.controller;

import com.barogagi.member.withdraw.service.WithdrawalReasonService;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 탈퇴", description = "회원 탈퇴 관련 API")
@RestController
@AllArgsConstructor
public class WithdrawController {

    private final WithdrawalReasonService withdrawalReasonService;

    @Operation(summary = "탈퇴 사유 조회 기능", description = "탈퇴 사유 조회 API입니다.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "잘못된 접근입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "D401", description = "탈퇴 사유 조회 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "D200", description = "탈퇴 사유 조회 성공하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON-500", description = "서버 오류가 발생했습니다.")
            })
    @GetMapping("/api/v1/withdrawal-reasons")
    public ApiResponse getWithdrawalReasons(@RequestHeader("API-KEY") String apiSecretKey) {
        return withdrawalReasonService.getWithdrawalReasons(apiSecretKey);
    }
}
