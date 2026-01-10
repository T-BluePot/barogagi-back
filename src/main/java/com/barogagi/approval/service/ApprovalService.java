package com.barogagi.approval.service;

import com.barogagi.approval.exception.ApprovalException;
import com.barogagi.approval.mapper.ApprovalMapper;
import com.barogagi.approval.vo.ApprovalCompleteVO;
import com.barogagi.approval.vo.ApprovalSendVO;
import com.barogagi.approval.vo.ApprovalVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.sendSms.dto.SendSmsVO;
import com.barogagi.sendSms.service.SendSmsService;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovalService {

    private final ApprovalMapper approvalMapper;
    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final AuthCodeService authCodeService;
    private final SendSmsService sendSmsService;

    @Autowired
    public ApprovalService(
                            ApprovalMapper approvalMapper,
                            Validator validator,
                            InputValidate inputValidate,
                            EncryptUtil encryptUtil,
                            AuthCodeService authCodeService,
                            SendSmsService sendSmsService
                            )
    {
        this.approvalMapper = approvalMapper;
        this.validator = validator;
        this.inputValidate = inputValidate;
        this.encryptUtil = encryptUtil;
        this.authCodeService = authCodeService;
        this.sendSmsService = sendSmsService;
    }

    public ApiResponse approvalTelSend(ApprovalSendVO approvalSendVO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(approvalSendVO.getApiSecretKey())) {
            throw new ApprovalException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(approvalSendVO.getTel())) {
            throw new ApprovalException(ErrorCode.EMPTY_DATA);
        }

        // 3. 처리
        // 인증번호를 DB에 INSERT 전에, 전에 발송된 기록들은 flag UPDATE 처리
        ApprovalVO approvalVO = new ApprovalVO();
        approvalVO.setCompleteYn("N");
        approvalVO.setType(approvalSendVO.getType());

        approvalSendVO.setTel(approvalSendVO.getTel().replaceAll("[^0-9]", ""));

        // 전화번호 암호화
        approvalVO.setTel(encryptUtil.hashEncodeString(approvalSendVO.getTel()));

        int updateResult = this.updateApprovalRecord(approvalVO);

        // 인증번호 생성
        String authCode = authCodeService.generateAuthCode();

        // 인증번호 메시지 발송
        SendSmsVO sendSmsVO = new SendSmsVO();
        sendSmsVO.setRecipientTel(approvalSendVO.getTel());
        String messageContent = "인증번호는 [" + authCode + "] 입니다.";
        sendSmsVO.setMessageContent(messageContent);
        boolean sendMessageResult = sendSmsService.sendSms(sendSmsVO);

        if(!sendMessageResult) {
            throw new ApprovalException(ErrorCode.FAIL_SEND_SMS);
        }

        // 인증번호 암호화
        approvalVO.setAuthCode(encryptUtil.hashEncodeString(authCode));

        approvalVO.setMessageContent(sendSmsVO.getMessageContent());
        int insertResult = this.insertApprovalRecord(approvalVO);

        if(insertResult <= 0) {
            throw new ApprovalException(ErrorCode.ERROR_SEND_SMS);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_SEND_SMS);
    }

    public ApiResponse approvalTelCheck(ApprovalCompleteVO approvalCompleteVO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(approvalCompleteVO.getApiSecretKey())) {
            throw new ApprovalException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(approvalCompleteVO.getAuthCode())
                || inputValidate.isEmpty(approvalCompleteVO.getTel())) {
            throw new ApprovalException(ErrorCode.EMPTY_DATA);
        }

        // 3. 전화번호 암호화
        ApprovalVO approvalVO = new ApprovalVO();
        approvalCompleteVO.setTel(approvalCompleteVO.getTel().replaceAll("[^0-9]", ""));
        approvalVO.setTel(encryptUtil.hashEncodeString(approvalCompleteVO.getTel()));
        approvalVO.setCompleteYn("N");
        approvalVO.setAuthCode(encryptUtil.hashEncodeString(approvalCompleteVO.getAuthCode()));
        approvalVO.setType(approvalCompleteVO.getType());

        // 4. 인증
        int updateResult = this.updateApprovalComplete(approvalVO);
        if(updateResult != 1){
            throw new ApprovalException(ErrorCode.FAIL_CHECK_SMS);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_CHECK_SMS);
    }

    public int updateApprovalRecord(ApprovalVO vo){
        return approvalMapper.updateApprovalRecord(vo);
    }

    public int insertApprovalRecord(ApprovalVO vo){
        return approvalMapper.insertApprovalRecord(vo);
    }

    public int updateApprovalComplete(ApprovalVO vo){
        return approvalMapper.updateApprovalComplete(vo);
    }
}
