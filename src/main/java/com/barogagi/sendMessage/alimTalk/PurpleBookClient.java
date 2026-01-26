package com.barogagi.sendMessage.alimTalk;

import com.barogagi.sendMessage.alimTalk.dto.AlimTalkRequest;
import com.barogagi.sendMessage.alimTalk.dto.PurpleBookResponse;
import com.barogagi.sendMessage.sms.dto.SendSmsVO;
import com.barogagi.sendMessage.sms.service.SmsSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurpleBookClient {

    private final SmsSendService smsSendService;

    private final WebClient webClient;

    @Value("${purplebook.api-key}")
    private String apiKey;

    @Value("${purplebook.alimtalk-url}")
    private String alimTalkUrl;

    public PurpleBookResponse withdrawal24hAlimTalk(String phone, Map<String, String> variables) {

        AlimTalkRequest request = AlimTalkRequest.builder()
                .apiKey(apiKey)
                .templateCode("WITHDRAWAL_NOTICE") // 템플릿 코드
                .phone(phone)
                .variables(variables)
                .build();

        return webClient.post()
                .uri(alimTalkUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PurpleBookResponse.class)
                .block();
    }

    // fallback SMS
    public boolean sendSms(String phone, String message) {
        SendSmsVO sendSmsVO = new SendSmsVO();
        sendSmsVO.setRecipientTel(phone);
        sendSmsVO.setMessageContent(message);
        return smsSendService.sendSms(sendSmsVO);
    }
}
