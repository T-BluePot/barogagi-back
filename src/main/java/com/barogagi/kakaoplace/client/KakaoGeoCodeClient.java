package com.barogagi.kakaoplace.client;

import com.barogagi.kakaoplace.dto.KakaoGeoCodeResDTO;
import com.barogagi.kakaoplace.dto.KakaoGeoCodeSearchResDTO;
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
public class KakaoGeoCodeClient {
    private static final Logger logger = LoggerFactory.getLogger(KakaoGeoCodeClient.class);

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<KakaoGeoCodeResDTO> convertKakaoGeoCode(String address) {
        String url = String.valueOf(UriComponentsBuilder
//                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build(false));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoGeoCodeSearchResDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoGeoCodeSearchResDTO.class
        );

        List<KakaoGeoCodeResDTO> body = response.getBody().getDocuments();
        return body;
    }
}
