package com.barogagi.member.oauth.join.service;

import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.oauth.join.dto.OAuth2UserDTO;
import com.barogagi.member.oauth.join.mapper.OAuth2UserMapper;
import com.barogagi.util.EncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KakaoOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(KakaoOAuth2UserService.class);
    private final OAuth2UserMapper oAuth2UserMapper;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Autowired
    private EncryptUtil encryptUtil;

    @Autowired
    private JoinService joinService;

    public KakaoOAuth2UserService(OAuth2UserMapper oAuth2UserMapper) {
        this.oAuth2UserMapper = oAuth2UserMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User kakaoUser = delegate.loadUser(userRequest);
        Map<String, Object> attr = kakaoUser.getAttributes();

        logger.info("attr={}", attr);

        // 1) 카카오 표준 속성 파싱 (이메일, 프로필 사진, 닉네임)
        String id = String.valueOf(attr.get("id")); // Long -> String 변환 안전하게
        Map<String, Object> kakaoAccount = (Map<String, Object>) attr.get("kakao_account");

        String email = "";
        String nickName = "";
        String profileImg = "";
        if(null != kakaoAccount) {
            // 이메일
            // 계정에 이메일이 존재하는지 (존재 : ture, 미존재 : false)
            boolean hasEmail = Boolean.TRUE.equals(kakaoAccount.get("has_email"));

            // 이메일 제공에 추가 동의가 필요한지 (필요 : true, 불필요 : false)
            boolean needAgree = Boolean.TRUE.equals(kakaoAccount.get("email_needs_agreement"));

            // 이메일 형식 유효성 (유효 : true, 무효 : false)
            boolean valid = Boolean.TRUE.equals(kakaoAccount.get("is_email_valid"));

            // 이메일 본인 확인(검증) 여부 (확인(검증)됨 : true, 확인(검증)안됨 : false)
            boolean verified = Boolean.TRUE.equals(kakaoAccount.get("is_email_verified"));

            if (hasEmail && !needAgree && valid && verified) {
                email = encryptUtil.encrypt((String) kakaoAccount.get("email"));
            }

            // 프로필
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if(null != profile) {
                nickName = (String) profile.get("nickname");
                profileImg = (String) profile.get("profile_image_url");
            }

            // id에 prefix 추가
            id = "provider=kakao" + id;
        }

        // 2) 우리 DB에 upsert
        try {
            // 카카오로 회원가입한 정보가 있는지 체크
            OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
            oAuth2UserDTO.setSub(id);
            oAuth2UserDTO.setEmail(email);
            oAuth2UserDTO.setJoinType("KAKAO");

            // 카카오로 회원가입한 정보가 있는지 조회
            OAuth2UserDTO userDTO = oAuth2UserMapper.findByOAuthSub(oAuth2UserDTO);

            // 없으면 insert
            if (userDTO == null) {
                logger.info("KAKAO 신규 회원");
                JoinDTO dto = new JoinDTO();
                dto.setUserId(id);
                dto.setEmail(email);
                dto.setNickName(nickName);
                dto.setProfileImg(profileImg);
                dto.setJoinType("KAKAO");

                int insertResult = joinService.insertMembershipInfo(dto);

                logger.info("KAKAO join result={}", insertResult);
            }
        } catch (Exception e) {
            logger.error("KAKAO OAuth 회원가입 중 오류 발생: {}", e.getMessage());
        }

        // 3) 컨트롤러/핸들러에서 공통으로 쓰기 쉽도록 key를 통일해서 리턴
        Map<String, Object> unified = new java.util.HashMap<>();
        unified.put("id", id);
        unified.put("email", email != null ? email : "");
        unified.put("name", nickName != null ? nickName : "");
        unified.put("picture", profileImg != null ? profileImg : "");

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                unified,
                "id"
        );
    }
}
