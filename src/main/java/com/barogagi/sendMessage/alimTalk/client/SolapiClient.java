package com.barogagi.sendMessage.alimTalk.client;

import com.barogagi.batch.dto.SendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolapiClient {

    private final WebClient webClient;

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.from}")
    private String from;

    @Value("${solapi.pf-id}")
    private String pfId;

    public String sendAlimTalk(SendDTO sendDTO) {

        Map<String, Object> kakaoOptions = new HashMap<>();
        kakaoOptions.put("pfId", pfId);
        kakaoOptions.put("templateId", sendDTO.getTemplateId());
        kakaoOptions.put("variables", sendDTO.getVariables());

        Map<String, Object> message = Map.of(
                "to", sendDTO.getTel(),
                "from", from,
                "kakaoOptions", kakaoOptions
        );

        Map<String, Object> request = Map.of(
                "messages", List.of(message)
        );

        return webClient.post()
                .uri("/messages/v4/send-many")
                .header("Authorization", generateAuthHeader())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new RuntimeException("API ERROR: " + body))
                )
                .bodyToMono(String.class)
                .block();
    }

    private String generateAuthHeader() {
        String date = String.valueOf(System.currentTimeMillis());
        String salt = UUID.randomUUID().toString();

        String data = date + salt;
        String signature = hmacSha256(data, apiSecret);

        return String.format(
                "HMAC-SHA256 ApiKey=%s, Date=%s, Salt=%s, Signature=%s",
                apiKey, date, salt, signature
        );
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
