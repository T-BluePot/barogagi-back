package com.barogagi.discord;

import com.barogagi.discord.dto.DiscordErrorMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class DiscordNotifier {

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendError(DiscordErrorMessage message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", message.format());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(webhookUrl, entity, String.class);
    }
}