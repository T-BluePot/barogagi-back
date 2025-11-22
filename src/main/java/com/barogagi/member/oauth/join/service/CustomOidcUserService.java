package com.barogagi.member.oauth.join.service;

import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.oauth.join.dto.OAuth2UserDTO;
import com.barogagi.member.oauth.join.mapper.OAuth2UserMapper;
import com.barogagi.util.EncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 1) OIDC 전용 서비스
@Service
public class CustomOidcUserService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);
    private final OAuth2UserMapper oAuth2UserMapper;

    @Autowired
    private EncryptUtil encryptUtil;

    @Autowired
    private JoinService joinService;

    public CustomOidcUserService(OAuth2UserMapper oAuth2UserMapper) {
        this.oAuth2UserMapper = oAuth2UserMapper;
    }

    @Override
    public org.springframework.security.oauth2.core.oidc.user.OidcUser loadUser(
            org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest userRequest) {

        logger.info("GOOGLE LOGIN");

        // 기본 동작으로 OIDC userInfo 먼저 로드
        org.springframework.security.oauth2.core.oidc.user.OidcUser user = super.loadUser(userRequest);

        var attr = user.getAttributes();

        logger.info("attr={}", attr);

        String sub     = asString(attr.get("sub"));
        String email   = asString(attr.get("email"));
        String name    = asString(attr.get("name"));
        String picture = asString(attr.get("picture"));

        logger.info("[OIDC] CustomOidcUserService.loadUser called. sub={}, email={}, name={}", sub, email, name);

        try {
            // 구글로 회원가입한 정보가 있는지 체크
            OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
            oAuth2UserDTO.setSub(sub);
            oAuth2UserDTO.setEmail(encryptUtil.encrypt(email));
            oAuth2UserDTO.setJoinType("GOOGLE");

            // 구글로 회원가입한 정보가 있는지 조회
            OAuth2UserDTO userDTO = oAuth2UserMapper.findByOAuthSub(oAuth2UserDTO);

            // 없으면 insert
            if (userDTO == null) {
                JoinDTO joinDTO = new JoinDTO();
                joinDTO.setUserId(sub);
                joinDTO.setEmail(encryptUtil.encrypt(email));
                joinDTO.setNickName(name);
                joinDTO.setJoinType("GOOGLE");
                joinDTO.setProfileImg(picture);

                int insertResult = joinService.insertMembershipInfo(joinDTO);

                logger.info("GOOGLE join result={}", insertResult);
            }

        } catch (Exception e) {
            logger.error("GOOGLE OAuth 회원가입 중 오류 발생: {}", e.getMessage());
        }
        return user;
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
}
