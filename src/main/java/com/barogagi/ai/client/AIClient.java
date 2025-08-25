package com.barogagi.ai.client;

import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.ai.dto.ChatMessage;
import com.barogagi.ai.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIClient {
    private static final Logger logger = LoggerFactory.getLogger(AIClient.class);

    @Value("${ai.api.base-url}")
    private String aiBaseUrl;

    @Value("${ai.api.path}")
    private String aiPath;

    @Value("${ai.api.key}")
    private String aiApiKey;

    @Value("${ai.prompt.system}")
    private String aiPrompt;

    @Value("${ai.model}")
    private String aiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public AIResDTO recommandPlace(AIReqWrapper aiReqWrapper) {
        String url = aiBaseUrl + aiPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(aiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 변환
        ChatRequest chatRequest = buildChatRequest(aiReqWrapper);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);

        ResponseEntity<AIResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                AIResDTO.class
        );

        return response.getBody();
    }

    // ai에게 요청 가능한 형태로 변경
    private ChatRequest buildChatRequest(AIReqWrapper wrapper) {
        // system 메시지
        ChatMessage systemMsg = ChatMessage.builder()
                .role("system")
                .content(aiPrompt)
                .build();

        // user 메시지: AIReqWrapper → 문자열 변환
        StringBuilder sb = new StringBuilder();
        sb.append("comment: ").append(wrapper.getComment()).append("\n");
        sb.append("tags: ").append(String.join(", ", wrapper.getTags())).append("\n\n");

        sb.append("places:\n");
        String placesText = wrapper.getPlaceList().stream()
                .map(p -> "- title: " + p.getTitle() + "\n  description: " + p.getDescription())
                .collect(Collectors.joining("\n"));
        sb.append(placesText);

        ChatMessage userMsg = ChatMessage.builder()
                .role("user")
                .content(sb.toString())
                .build();

        return ChatRequest.builder()
                .model(aiModel)
                .messages(List.of(systemMsg, userMsg))
                .max_tokens(500)
                .build();
    }
}
