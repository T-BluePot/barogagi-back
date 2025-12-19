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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "인증번호를 발송할 전화번호를 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송에 성공하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "102", description = "오류가 발생하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "103", description = "인증번호 발송에 실패하였습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/send")
    public ApiResponse approvalTelSend(@RequestBody ApprovalSendVO approvalSendVO) {

        logger.info("CALL /api/v1/verification-codes/send");
        logger.info("[input] API_SECRET_KEY={}", approvalSendVO.getApiSecretKey());

        ApprovalVO approvalVO = new ApprovalVO();

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {

            if(approvalSendVO.getApiSecretKey().equals(API_SECRET_KEY)) {

                if(inputValidate.isEmpty(approvalSendVO.getTel())){
                    resultCode = "101";
                    message = "인증번호를 발송할 전화번호를 입력해주세요.";

                } else{

                    // 전화번호
                    String recipientTel = approvalSendVO.getTel();

                    // 인증번호를 DB에 INSERT 전에, 전에 발송된 기록들은 flag UPDATE 처리
                    approvalVO.setCompleteYn("N");
                    approvalVO.setType(approvalSendVO.getType());

                    // 전화번호 암호화
                    approvalVO.setTel(encryptUtil.hashEncodeString(approvalSendVO.getTel()));

                    int updateResult = approvalService.updateApprovalRecord(approvalVO);
                    logger.info("@@ updateResult={}", updateResult);

                    // 인증번호 생성
                    String authCode = authCodeService.generateAuthCode();
                    logger.info("@@ authCode={}", authCode);

                    // 인증번호 메시지 발송
                    SendSmsVO sendSmsVO = new SendSmsVO();
                    sendSmsVO.setRecipientTel(recipientTel);
                    String messageContent = "인증번호는 [" + authCode + "] 입니다.";
                    sendSmsVO.setMessageContent(messageContent);
                    boolean sendMessageResult = sendSmsService.sendSms(sendSmsVO);
                    logger.info("@@ sendMessageResult={}", sendMessageResult);

                    // 인증번호 암호화
                    approvalVO.setAuthCode(encryptUtil.hashEncodeString(authCode));

                    // 인증번호를 DB에 insert
                    if(sendMessageResult){
                        approvalVO.setMessageContent(sendSmsVO.getMessageContent());
                        int insertResult = approvalService.insertApprovalRecord(approvalVO);
                        logger.info("@@ insertResult={}", insertResult);
                        if(insertResult > 0) {
                            // 인증번호 발송 로직
                            resultCode = "200";
                            message = "인증번호 발송에 성공하었습니다.";

                        } else{
                            resultCode = "102";
                            message = "오류가 발생하였습니다.";
                        }
                    } else {
                        resultCode = "103";
                        message = "인증번호 발송에 실패하였습니다.";
                    }
                }
            } else {
                resultCode = "100";
                message = "잘못된 접근입니다.";
            }
        } catch (Exception e) {
            resultCode = "400";
            message = "오류가 발생하였습니다.";
            throw new RuntimeException(e);
        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return apiResponse;
    }

    @Operation(summary = "인증번호 일치 여부 확인", description = "휴대전화번호에 발송된 인증번호와 입력된 인증번호가 동일한지 확인." +
            "<br> 회원가입 시 사용할 경우 type 값을 JOIN-MEMBERSHIP 값으로 넣어주세요." +
            "<br> authCode에는 인증번호를 넣어주세요." +
            "<br> tel에는 전화번호를 넣어주세요.",
            responses =  {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "101", description = "전화번호 또는 인증번호 값을 입력해주세요."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증이 완료되었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "300", description = "인증이 실패하었습니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "100", description = "API SECRET KEY 불일치"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "오류가 발생하였습니다.")
            })
    @PostMapping("/verify")
    public ApiResponse approvalTelCheck(@RequestBody ApprovalCompleteVO approvalCompleteVO) {

        logger.info("CALL /api/v1/verification-codes/verify");
        logger.info("[input] API_SECRET_KEY={}", approvalCompleteVO.getApiSecretKey());

        ApprovalVO approvalVO = new ApprovalVO();

        ApiResponse apiResponse = new ApiResponse();
        String resultCode = "";
        String message = "";

        try {
            if(approvalCompleteVO.getApiSecretKey().equals(API_SECRET_KEY)) {
                if(inputValidate.isEmpty(approvalCompleteVO.getAuthCode()) || inputValidate.isEmpty(approvalCompleteVO.getTel())){
                    resultCode = "101";
                    message = "전화번호 또는 인증번호 값을 입력해주세요.";

                } else{
                    logger.info("@@@@ authCode = {}", approvalCompleteVO.getAuthCode());

                    // 전화번호 암호화
                    approvalVO.setTel(encryptUtil.hashEncodeString(approvalCompleteVO.getTel()));
                    approvalVO.setCompleteYn("N");
                    approvalVO.setAuthCode(encryptUtil.hashEncodeString(approvalCompleteVO.getAuthCode()));
                    approvalVO.setType(approvalCompleteVO.getType());

                    logger.info("authcode = {}", encryptUtil.hashEncodeString(approvalCompleteVO.getAuthCode()));

                    int updateResult = approvalService.updateApprovalComplete(approvalVO);
                    logger.info("@@ updateResult={}", updateResult);

                    if(updateResult == 1){
                        resultCode = "200";
                        message = "인증이 완료되었습니다.";
                    } else {
                        resultCode = "300";
                        message = "인증에 실패하였습니다.";
                    }
                }

            } else {
                resultCode = "100";
                message = "잘못된 접근입니다.";
            }

        } catch (Exception e) {
            resultCode = "400";
            message = "오류가 발생하였습니다.";
            throw new RuntimeException(e);

        } finally {
            apiResponse.setResultCode(resultCode);
            apiResponse.setMessage(message);
        }
        return apiResponse;
    }
}
