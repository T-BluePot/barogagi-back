package com.barogagi.sendMessage.email.service;

import com.barogagi.sendMessage.email.dto.SendMailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final AsyncSmtpMailService asyncSmtpMailService;
    private final SpringTemplateEngine templateEngine;

    public boolean sendWithdrawlEmail(SendMailDTO sendMailDTO, Map<String, String> variables) {

        sendMailDTO.setBody(buildWithdrawlHtml(variables));

        // 실제 메일 발송
        return asyncSmtpMailService.sendMailAsync(sendMailDTO);
    }

    public String buildWithdrawlHtml(Map<String, String> variables) {
        Context context = new Context();

        // Map → Thymeleaf 변수 세팅
        variables.forEach(context::setVariable);

        // templates/mail/withdrawal-notice.html 읽어서 렌더링
        return templateEngine.process("mail/withdrawal-notice", context);
    }
}
