package com.barogagi.member.join.oauth.service;

import com.barogagi.member.join.oauth.dto.OAuthLinkDTO;
import com.barogagi.member.join.oauth.enums.Environment;
import com.barogagi.member.join.oauth.enums.Type;
import com.barogagi.member.join.oauth.exception.OAuthException;
import com.barogagi.response.ApiResponse;
import com.barogagi.util.Validator;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final Validator validator;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public ApiResponse selectOAuthLink(OAuthLinkDTO oAuthLinkDTO) {

        // 1. API SECRET KEY 일치 여부 확인
        if(!validator.apiSecretKeyCheck(oAuthLinkDTO.getApiSecretKey())) {
            throw new OAuthException(ErrorCode.NOT_EQUAL_API_SECRET_KEY);
        }

        // 2. 서버별 주소
        List<String> addresses = Arrays.asList(allowedOrigins.split(","));

        // LOCAL
        String localAddress = addresses.get(0);
        // TEST
        String testAddress = addresses.get(1);
        // REAL
        String realAddress = addresses.get(2);

        // 3. OAuth Link
        Environment environment = oAuthLinkDTO.getEnvironment();
        Type type = oAuthLinkDTO.getType();
        String uri = "/oauth2/authorization";
        String link = "";

        if(environment.equals(Environment.LOCAL)) {  // 로컬 서버
            link = localAddress + uri;
            link = switch (type) {
                case Google -> link + "/google";
                case Kakao -> link + "/kakao";
                case Naver -> link + "/naver";
            };
        } else if(environment.equals(Environment.TEST)) {  // 테스트 서버
            link = testAddress + uri;
            link = switch (type) {
                case Google -> link + "/google";
                case Kakao -> link + "/kakao";
                case Naver -> link + "/naver";
            };
        } else if(environment.equals(Environment.PROD)) {  // 실서버
            link = realAddress + uri;
            link = switch (type) {
                case Google -> link + "/google";
                case Kakao -> link + "/kakao";
                case Naver -> link + "/naver";
            };
        }

        return ApiResponse.resultData(link, "L200", "조회 성공하였습니다.");
    }
}
