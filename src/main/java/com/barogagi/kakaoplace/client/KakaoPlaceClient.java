package com.barogagi.kakaoplace.client;

import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.kakaoplace.dto.KakaoPlaceSearchResDTO;
import com.barogagi.schedule.command.service.ScheduleCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class KakaoPlaceClient {
    private static final Logger logger = LoggerFactory.getLogger(KakaoPlaceClient.class);

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<KakaoPlaceResDTO> searchKakaoPlace(String query, String x, String y, int radius, int limitPlace) {
        String url = String.valueOf(UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("radius", radius)
                .queryParam("size", limitPlace)
                .build(false));

//        logger.info("#$# url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
//        logger.info("#$# Request Headers: {}", headers);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoPlaceSearchResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoPlaceSearchResDTO.class
        );
//        logger.info("#$# response={}", response);
//        logger.info("#$# response.getDocuments={}", response.getBody().getDocuments());

        List<KakaoPlaceResDTO> body = response.getBody().getDocuments();
        return body;
    }
}
