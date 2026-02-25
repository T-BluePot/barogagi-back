package com.barogagi.approval.controller;

import com.barogagi.approval.service.ApprovalService;
import com.barogagi.approval.vo.ApprovalCompleteVO;
import com.barogagi.approval.vo.ApprovalSendVO;
import com.barogagi.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "인증 API")
@RestController
@RequestMapping("/api/v1/verification-codes")
public class ApprovalController {

    private final ApprovalService approvalService;

    @Autowired
    public ApprovalController(ApprovalService approvalService){
        this.approvalService = approvalService;
    }

    @Operation(summary = "인증번호 발송 기능", description = "휴대전화번호로 인증번호 발송하는 기능입니다. <br> 회원가입 시 사용할 경우 type 값을 JOIN-MEMBERSHIP 값으로 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A200", description = "인증번호 발송에 성공하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A102", description = "인증문자 발송 중 오류가 발생하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A103", description = "인증번호 발송에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/send")
    public ApiResponse approvalTelSend(@RequestHeader("API-KEY") String apiSecretKey, @RequestBody ApprovalSendVO approvalSendVO) {
        return approvalService.approvalTelSend(apiSecretKey, approvalSendVO);
    }

    @Operation(summary = "인증번호 일치 여부 확인 기능", description = "휴대전화번호에 발송된 인증번호와 입력된 인증번호가 동일한지 확인." +
            "<br> 회원가입 시 사용할 경우 type 값을 JOIN-MEMBERSHIP 값으로 넣어주세요." +
            "<br> authCode에는 인증번호를 넣어주세요." +
            "<br> tel에는 전화번호를 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A200", description = "인증이 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A300", description = "인증이 실패하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "A100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/verify")
    public ApiResponse approvalTelCheck(@RequestHeader("API-KEY") String apiSecretKey, @RequestBody ApprovalCompleteVO approvalCompleteVO) {
        return approvalService.approvalTelCheck(apiSecretKey, approvalCompleteVO);
    }
}
