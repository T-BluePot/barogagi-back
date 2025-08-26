package com.barogagi.ai.client;

import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.ai.dto.ChatMessage;
import com.barogagi.ai.dto.ChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static com.barogagi.util.HtmlUtils.stripCodeFence;

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
    private static final ObjectMapper OM = new ObjectMapper();

    public AIResDTO recommandPlace(AIReqWrapper aiReqWrapper) {
        String url = aiBaseUrl + aiPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(aiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ChatRequest chatRequest = buildChatRequest(aiReqWrapper);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        String body = response.getBody();

        ObjectMapper om = new ObjectMapper();
        try {
            // 1) content 추출
            JsonNode root = om.readTree(body);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            String content = contentNode.asText();

            // 2) 코드펜스/여분 공백 제거
            content = stripCodeFence(content);

            // 3) content(JSON 문자열) -> DTO
            AIResDTO dto = om.readValue(content, AIResDTO.class);
            return dto;

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // JsonMappingException 포함해서 모두 여기서 처리됨
            return null; // 혹은 throw new IllegalStateException("AI 응답 파싱 실패", e);
        }
    }

//        String url = aiBaseUrl + aiPath;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(aiApiKey);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//
//        ChatRequest chatRequest = buildChatRequest(aiReqWrapper);
//        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);
//
//        // --- 최소 요청 로그 ---
//        logger.info("#$# AI url={}", url);
//        logger.info("#$# AI req: tags={}, commentLen={}, placeList={}",
//                aiReqWrapper.getTags() == null ? 0 : aiReqWrapper.getTags().size(),
//                aiReqWrapper.getComment() == null ? 0 : aiReqWrapper.getComment().length(),
//                aiReqWrapper.getPlaceList() == null ? 0 : aiReqWrapper.getPlaceList().size());
//
//        // --- 응답 원문(String)으로 1회 호출 → 로그 찍고 → DTO로 매핑 ---
//        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//        logger.info("#$# AI resp status={}", resp.getStatusCode());
//        String raw = resp.getBody();
//        logger.info("#$# AI resp raw (preview): {}", raw == null
//                ? "null"
//                : (raw.length() <= 1000 ? raw : raw.substring(0, 1000) + "...(truncated)"));
//
//        // --- DTO 매핑 (실패해도 예외만 로그) ---
//        try {
//            AIResDTO dto = OM.readValue(raw, AIResDTO.class);
//            logger.info("#$# AI resp DTO: recommandPlaceNum={}, aiDescription='{}'",
//                    dto.getRecommandPlaceNum(), dto.getAiDescription());
//            return dto;
//        } catch (Exception e) {
//            logger.warn("#$# parse to AIResDTO failed: {}", e.getMessage());
//            return null;
//        }
//    }

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
