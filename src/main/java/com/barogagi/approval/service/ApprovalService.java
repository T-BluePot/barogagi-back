package com.barogagi.approval.service;

import com.barogagi.approval.domain.ApprovalNumInfo;
import com.barogagi.approval.dto.ApprovalCheckDTO;
import com.barogagi.approval.exception.ApprovalException;
import com.barogagi.approval.vo.ApprovalCompleteVO;
import com.barogagi.approval.vo.ApprovalSendVO;
import com.barogagi.approval.vo.ApprovalVO;
import com.barogagi.response.ApiResponse;
import com.barogagi.sendMessage.sms.dto.SendSmsVO;
import com.barogagi.sendMessage.sms.service.SmsSendService;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.InputValidate;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final Validator validator;
    private final InputValidate inputValidate;
    private final EncryptUtil encryptUtil;
    private final AuthCodeService authCodeService;
    private final SmsSendService smsSendService;
    private final ApprovalTxService approvalTxService;

    public ApiResponse approvalTelSend(String apiSecretKey, ApprovalSendVO approvalSendVO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new ApprovalException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(approvalSendVO.getTel())) {
            throw new ApprovalException(ErrorCode.EMPTY_DATA);
        }

        // 3. 1분 내로 시도했을 경우 잠시 후 발송
        ApprovalCheckDTO approvalCheckDTO = new ApprovalCheckDTO();
        approvalCheckDTO.setTel(encryptUtil.hashEncodeString(approvalSendVO.getTel().replaceAll("[^0-9]", "")));
        approvalCheckDTO.setCompleteYn("N");
        approvalCheckDTO.setType(approvalSendVO.getType());
        approvalCheckDTO.setRegDate(LocalDateTime.now().minusMinutes(1));
        List<ApprovalNumInfo> approvalCheckDTOList = approvalTxService.findApprovalNumInfo(approvalCheckDTO);
        if(!approvalCheckDTOList.isEmpty()) {
            throw new ApprovalException(ErrorCode.NOT_ACCESS_SEND_APPROVAL);
        }

        // 4. 처리
        // 인증번호를 DB에 INSERT 전에, 전에 발송된 기록들은 flag UPDATE 처리
        ApprovalVO approvalVO = new ApprovalVO();
        approvalVO.setCompleteYn("N");
        approvalVO.setType(approvalSendVO.getType());

        approvalSendVO.setTel(approvalSendVO.getTel().replaceAll("[^0-9]", ""));

        // 전화번호 암호화
        approvalVO.setTel(encryptUtil.hashEncodeString(approvalSendVO.getTel()));

        boolean updateResult = approvalTxService.cancelApproval(approvalVO.getTel(), approvalVO.getType(), approvalVO.getCompleteYn());

        // 인증번호 생성
        String authCode = authCodeService.generateAuthCode();

        // 인증번호 메시지 발송
        SendSmsVO sendSmsVO = new SendSmsVO();
        sendSmsVO.setRecipientTel(approvalSendVO.getTel());
        String messageContent = "[핏플(fitpl)]\r\n인증번호: " + authCode;
        sendSmsVO.setMessageContent(messageContent);
        boolean sendMessageResult = smsSendService.sendSms(sendSmsVO);

        if(!sendMessageResult) {
            throw new ApprovalException(ErrorCode.FAIL_SEND_APPROVAL);
        }

        // 인증번호 암호화
        approvalVO.setAuthCode(encryptUtil.hashEncodeString(authCode));

        approvalVO.setMessageContent(sendSmsVO.getMessageContent());
        boolean insertResult = approvalTxService.insertApprovalRecord(approvalVO);

        if(!insertResult) {
            throw new ApprovalException(ErrorCode.ERROR_SEND_APPROVAL);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_SEND_APPROVAL);
    }

    public ApiResponse approvalTelCheck(String apiSecretKey, ApprovalCompleteVO approvalCompleteVO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(apiSecretKey)) {
            throw new ApprovalException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 필수 입력값 확인
        if(inputValidate.isEmpty(approvalCompleteVO.getAuthCode())
                || inputValidate.isEmpty(approvalCompleteVO.getTel())) {
            throw new ApprovalException(ErrorCode.EMPTY_DATA);
        }

        // 3. 인증
        boolean updateResult = approvalTxService.updateApprovalComplete(
                encryptUtil.hashEncodeString(approvalCompleteVO.getTel().replaceAll("[^0-9]", "")),
                approvalCompleteVO.getType(),
                "N",
                encryptUtil.hashEncodeString(approvalCompleteVO.getAuthCode())
        );

        if(!updateResult){
            throw new ApprovalException(ErrorCode.FAIL_CHECK_APPROVAL);
        }

        return ApiResponse.result(ErrorCode.SUCCESS_CHECK_APPROVAL);
    }
}
