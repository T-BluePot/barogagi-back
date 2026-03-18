package com.barogagi.sendMessage.email.service;

import com.barogagi.sendMessage.email.dto.SendMailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final AsyncSmtpMailService asyncSmtpMailService;

    public boolean sendWithdrawlEmail(SendMailDTO sendMailDTO, Map<String, String> variables) {
        boolean sendResult = false;
        try {
            sendMailDTO.setBody(buildWithdrawlMessage(variables));

            // 실제 메일 발송
            asyncSmtpMailService.sendMailAsync(sendMailDTO);

            sendResult = true;
        } catch (Exception e) {
            log.error("메일 발송 실패: {}", sendMailDTO.getTo(), e);
        }
        return sendResult;
    }

    public String buildWithdrawlMessage(Map<String, String> variables) {
        return String.format("탈퇴 전환 안내\n" +
                "\n" +
                "안녕하세요, %s입니다.\n" +
                "\n" +
                "고객님의 탈퇴 신청이 접수되어 %s시간 후 계정이 탈퇴 상태로 전환될 예정입니다.\n" +
                "\n" +
                "■ 탈퇴 전환 예정일 : %s\n" +
                "\n" +
                "탈퇴 전환 이후에는 서비스 이용이 제한되며, 계정 정보는 관련 법령에 따라 처리됩니다.\n" +
                "\n" +
                "탈퇴를 원하지 않으실 경우, 전환 전까지 %s을 통해 탈퇴를 취소하실 수 있습니다.",
                variables.get("serviceName"), variables.get("afterHours"),
                variables.get("withdrawDay"), variables.get("cancelMethod")
                );
    }
}
