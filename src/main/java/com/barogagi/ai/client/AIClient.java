package com.barogagi.ai.client;

import com.barogagi.ai.constant.AiPrompt;
import com.barogagi.ai.dto.AIReqWrapper;
import com.barogagi.ai.dto.AIResDTO;
import com.barogagi.ai.dto.ChatMessage;
import com.barogagi.ai.dto.ChatRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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

    @Value("${ai.model}")
    private String aiModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper OM = new ObjectMapper();


    /**
     * 2차 AI 호출: 카카오 장소 목록에서 추천 장소 선정 + 한줄 소개
     */
    public AIResDTO recommandPlace(AIReqWrapper aiReqWrapper) {
        String url = aiBaseUrl + aiPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(aiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ChatRequest chatRequest = buildChatRequest(aiReqWrapper);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        String body = response.getBody();

        try {
            JsonNode root = OM.readTree(body);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = stripCodeFence(content);
            return OM.readValue(content, AIResDTO.class);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("AI 추천 응답 파싱 실패: error={}", e.getMessage());
            return null;
        }
    }

    private ChatRequest buildChatRequest(AIReqWrapper wrapper) {
        ChatMessage systemMsg = ChatMessage.builder()
                .role("system")
                .content(AiPrompt.RECOMMEND_PLACE_SYSTEM)
                .build();

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


    /**
     * 1차 AI 호출: Tavily 웹 검색 결과에서 실제 장소명 추출
     */
    public List<String> extractPlaceNames(String webContent, String categoryNm, String regionName,
                                          List<String> tagNames, int maxCount) {

        String tagStr = (tagNames != null && !tagNames.isEmpty())
                ? String.join(", ", tagNames)
                : categoryNm;

        String prompt = String.format(AiPrompt.EXTRACT_PLACE_NAMES_USER,
                regionName, categoryNm, categoryNm, tagStr, maxCount, webContent);

        try {
            String aiResponse = callAIForText(AiPrompt.EXTRACT_PLACE_NAMES_SYSTEM, prompt, 300);

            if (aiResponse == null || aiResponse.isBlank()) {
                logger.warn("AI 장소명 추출 응답이 비어있습니다.");
                return Collections.emptyList();
            }

            String cleaned = stripCodeFence(aiResponse).trim();
            List<String> placeNames = OM.readValue(cleaned, new TypeReference<List<String>>() {});

            logger.info("AI 장소명 추출 완료: count={}, names={}", placeNames.size(), placeNames);
            return placeNames;

        } catch (Exception e) {
            logger.error("AI 장소명 추출 실패: error={}", e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * AI에 프롬프트를 보내고 content 텍스트를 그대로 반환
     */
    private String callAIForText(String systemPrompt, String userPrompt, int maxTokens) {
        String url = aiBaseUrl + aiPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(aiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ChatRequest chatRequest = ChatRequest.builder()
                .model(aiModel)
                .messages(List.of(
                        ChatMessage.builder().role("system").content(systemPrompt).build(),
                        ChatMessage.builder().role("user").content(userPrompt).build()
                ))
                .max_tokens(maxTokens)
                .build();

        try {
            HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root = OM.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            logger.error("callAIForText 실패: error={}", e.getMessage());
            return null;
        }
    }
}