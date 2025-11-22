package com.barogagi.member.oauth.join.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class DelegatingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final NaverOAuth2UserService naverOAuth2UserService;
    private final KakaoOAuth2UserService kakaoOAuth2UserService;
    private final DefaultOAuth2UserService fallback = new DefaultOAuth2UserService();

    public DelegatingOAuth2UserService(
            NaverOAuth2UserService naverOAuth2UserService,
            KakaoOAuth2UserService kakaoOAuth2UserService
    ) {
        this.naverOAuth2UserService = naverOAuth2UserService;
        this.kakaoOAuth2UserService = kakaoOAuth2UserService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String regId = userRequest.getClientRegistration().getRegistrationId();
        if ("naver".equalsIgnoreCase(regId)) {
            return naverOAuth2UserService.loadUser(userRequest);
        }
        if ("kakao".equalsIgnoreCase(regId)) {
            return kakaoOAuth2UserService.loadUser(userRequest);
        }
        // 그 외(혹시 추가될 다른 OAuth2 공급자)는 기본 처리
        return fallback.loadUser(userRequest);
    }
}
