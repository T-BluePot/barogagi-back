package com.barogagi.discord;

import com.barogagi.discord.dto.DiscordErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class DiscordNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotifier.class);

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate;

    public DiscordNotifier() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Async
    public void sendError(DiscordErrorMessage message) {

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", message.format());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, entity, String.class);

        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패: {}", e.getMessage());
        }
    }
}