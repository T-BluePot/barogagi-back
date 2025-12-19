package com.barogagi.approval.controller;

import com.barogagi.approval.service.ApprovalService;
import com.barogagi.approval.service.AuthCodeService;
import com.barogagi.approval.vo.ApprovalCompleteVO;
import com.barogagi.approval.vo.ApprovalSendVO;
import com.barogagi.approval.vo.ApprovalVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.sendSms.dto.SendSmsVO;
import com.barogagi.sendSms.service.SendSmsService;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "인증 API")
@RestController
@RequestMapping("/api/v1/verification-codes")
public class ApprovalController {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);

    @Autowired
    private InputValidate inputValidate;

    @Autowired
    private EncryptUtil encryptUtil;

    @Autowired
    private AuthCodeService authCodeService;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private SendSmsService sendSmsService;

    private final String API_SECRET_KEY;

    @Autowired
    public ApprovalController(Environment environment){
        this.API_SECRET_KEY = environment.getProperty("api.secret-key");
    }

    @Operation(summary = "인증번호 발송", description = "휴대전화번호로 인증번호 발송하는 기능입니다. <br> 회원가입 시 사용할 경우 type 값을 JOIN-MEMBERSHIP 값으로 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송에 성공하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "오류가 발생하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "인증번호 발송에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/send")
    public ApiResponse approvalTelSend(@RequestBody ApprovalSendVO approvalSendVO) {
        return approvalService.approvalTelSend(approvalSendVO);
    }

    @Operation(summary = "인증번호 일치 여부 확인", description = "휴대전화번호에 발송된 인증번호와 입력된 인증번호가 동일한지 확인." +
            "<br> 회원가입 시 사용할 경우 type 값을 JOIN-MEMBERSHIP 값으로 넣어주세요." +
            "<br> authCode에는 인증번호를 넣어주세요." +
            "<br> tel에는 전화번호를 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "정보를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증이 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "인증이 실패하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/verify")
    public ApiResponse approvalTelCheck(@RequestBody ApprovalCompleteVO approvalCompleteVO) {
        return approvalService.approvalTelCheck(approvalCompleteVO);
    }
}
