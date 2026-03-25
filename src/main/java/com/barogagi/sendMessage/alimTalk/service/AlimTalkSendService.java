package com.barogagi.sendMessage.alimTalk.service;

import com.barogagi.sendMessage.alimTalk.client.SolapiClient;
import com.barogagi.sendMessage.sms.dto.SendSmsVO;
import com.barogagi.sendMessage.sms.service.SmsSendService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimTalkSendService {

    private final SolapiClient solapiClient;
    private final SmsSendService smsSendService;

    @Retry(name = "alimTalkRetry")
    @CircuitBreaker(name = "alimTalkCircuit", fallbackMethod = "fallbackSms")
    public boolean sendWithdrawalAlimTalk(String tel, Map<String, String> variables) {

        // 👉 1. SolapiClient 호출
        String response = solapiClient.sendAlimTalk(tel, variables);

        // 👉 2. 성공 여부 체크
        if (response == null || !response.contains("groupId")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean fallbackSms(String tel, Map<String, String> variables, Throwable t) {
        log.error("알림톡 실패 → SMS fallback", t);
        String message = buildSmsMessage(variables);

        SendSmsVO sendSmsVO = new SendSmsVO();
        sendSmsVO.setRecipientTel(tel);
        sendSmsVO.setMessageContent(message);
        return smsSendService.sendSms(sendSmsVO);
    }

    public String buildSmsMessage(Map<String, String> variables) {
        return String.format("안녕하세요, %s 입니다.\n" +
                "\n" +
                "고객님의 탈퇴 신청이 접수되어 %s시간 후 계정이 탈퇴 상태로 전환될 예정입니다.\n" +
                "\n" +
                "■ 탈퇴 전환 예정일 : %s\n" +
                "\n" +
                "탈퇴 전환 이후에는 서비스 이용이 제한되며, 계정 정보는 관련 법령에 따라 처리됩니다.\n" +
                "\n" +
                "탈퇴를 원하지 않으실 경우, 전환 전까지 %s을 통해 탈퇴를 취소하실 수 있습니다.",
                variables.get("serviceName"), variables.get("afterHours"),
                variables.get("withdrawDay"), variables.get("cancelMethod"));
    }
}
