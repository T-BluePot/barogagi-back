package com.barogagi.naverbolg.client;

import com.barogagi.kakaoplace.client.KakaoPlaceClient;
import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.kakaoplace.dto.KakaoPlaceSearchResDTO;
import com.barogagi.naverbolg.dto.NaverBlogResDTO;
import com.barogagi.naverbolg.dto.NaverBlogSearchResDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class NaverBlogClient {
    private static final Logger logger = LoggerFactory.getLogger(NaverBlogClient.class);

    @Value("${naver.x-naver-client-id}")
    private String xNaverClientId;

    @Value("${naver.x-naver-client-secret}")
    private String xNaverClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    // https://openapi.naver.com/v1/search/blog.json?query=고궁의아침 강남구&display=5

    public List<NaverBlogResDTO> searchNaverBlog(String query, int display) {
        String url = String.valueOf(UriComponentsBuilder
                .fromHttpUrl("https://openapi.naver.com/v1/search/blog.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .build(false));
        logger.info("#$# url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", xNaverClientId);
        headers.set("X-Naver-Client-Secret", xNaverClientSecret);
        //headers.set("Content-Type", "application/json;charset=UTF-8 ");
        logger.info("#$# Request Headers: {}", headers);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<NaverBlogSearchResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverBlogSearchResDTO.class
        );
        logger.info("#$# response.getBody()={}", response.getBody());
        logger.info("#$# response.getBody().getItems()={}", response.getBody().getItems());
        List<NaverBlogResDTO> body = response.getBody().getItems();

        return body;
    }

}
