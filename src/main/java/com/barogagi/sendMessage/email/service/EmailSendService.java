package com.barogagi.sendMessage.email.service;

import com.barogagi.batch.dto.SendDTO;
import com.barogagi.sendMessage.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final AsyncSmtpMailService asyncSmtpMailService;
    private final SpringTemplateEngine templateEngine;
    private final CommonService commonService;

    public boolean sendWithdrawlEmail(SendDTO sendDTO) {

        try {
            sendDTO.getSendMailDTO().setBody(buildWithdrawlHtml(sendDTO.getVariables()));

            if(commonService.isProd()) {  // 실서버에서만 이메일 발송
                // 실제 메일 발송
                CompletableFuture<Boolean> futureResult = asyncSmtpMailService.sendMailAsync(sendDTO.getSendMailDTO());

                // 발송 결과를 기다림
                boolean result = futureResult.get(); // 필요 시 timeout 설정 가능
                if (!result) {
                    log.error("메일 발송 실패: {}", sendDTO.getSendMailDTO().getTo());
                }
                return result;
            } else {  // 개발/테스트 서버에서는 항상 성공 처리
                return true;
            }

        } catch (Exception e) {
            log.error("이메일 발송 실패", e);
            return false;
        }
    }

    public String buildWithdrawlHtml(Map<String, String> variables) {
        Context context = new Context();

        // Map → Thymeleaf 변수 세팅
        variables.forEach(context::setVariable);

        // templates/mail/withdrawal-notice.html 읽어서 렌더링
        return templateEngine.process("mail/withdrawal-notice", context);
    }
}
