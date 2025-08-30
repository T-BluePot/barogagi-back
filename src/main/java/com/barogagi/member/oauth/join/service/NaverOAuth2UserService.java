package com.barogagi.member.oauth.join.service;

import com.barogagi.member.basic.join.dto.JoinDTO;
import com.barogagi.member.basic.join.service.JoinService;
import com.barogagi.member.oauth.join.dto.OAuth2UserDTO;
import com.barogagi.member.oauth.join.mapper.OAuth2UserMapper;
import com.barogagi.util.EncryptUtil;
import org.checkerframework.checker.units.qual.N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);
    private final OAuth2UserMapper oAuth2UserMapper;

    @Autowired
    private EncryptUtil encryptUtil;

    @Autowired
    private JoinService joinService;

    public NaverOAuth2UserService(OAuth2UserMapper oAuth2UserMapper) {
        this.oAuth2UserMapper = oAuth2UserMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {

        String nameAttributeKey;  // getName()에서 쓸 키(id)

        OAuth2User user = super.loadUser(req);

        // provider 구분 (여기서는 naver일 때만 처리)
        String registrationId = req.getClientRegistration().getRegistrationId();
        if (!"naver".equals(registrationId)) {
            return user; // 다른 provider는 건드리지 않음
        }

        Map<String, Object> attrs = user.getAttributes();
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = (Map<String, Object>) attrs.get("response");

        logger.info("resp={}", resp);
        if (resp == null) {
            throw new IllegalStateException("Naver userinfo has no 'response' field");
        }

        nameAttributeKey = "id"; // 네이버는 id가 고유 식별자

        String id = str(resp.get("id"));
        String nickName = str(resp.get("nickname"));
        String profileImg = str(resp.get("profile_image"));
        String gender = str(resp.get("gender"));
        String email = str(resp.get("email"));
        String birthday = str(resp.get("birthday"));
        String birthyear = str(resp.get("birthyear"));

        logger.info("id={}", id);
        logger.info("nickName={}", nickName);
        logger.info("profileImg={}", profileImg);
        logger.info("gender={}", gender);
        logger.info("email={}", email);
        logger.info("birthday={}", birthday);
        logger.info("birthyear={}", birthyear);

        try {
            // 네이버로 회원가입한 정보가 있는지 체크
            OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
            oAuth2UserDTO.setSub(id);
            oAuth2UserDTO.setEmail(encryptUtil.encrypt(email));
            oAuth2UserDTO.setJoinType("NAVER");

            // 네이버로 회원가입한 정보가 있는지 조회
            OAuth2UserDTO userDTO = oAuth2UserMapper.findByOAuthSub(oAuth2UserDTO);

            // 없으면 insert
            if (userDTO == null) {
                JoinDTO joinDTO = new JoinDTO();
                joinDTO.setUserId(id);
                joinDTO.setEmail(encryptUtil.encrypt(email));
                joinDTO.setNickName(nickName);
                joinDTO.setJoinType("NAVER");
                joinDTO.setProfileImg(profileImg);

                // gender(성별) : M(남성), F(여성), U(미설정)
                if(null != gender) {
                    if(gender.equals("M")) {  // 남성
                        joinDTO.setGender("M");
                    } else if(gender.equals("F")) {  // 여성
                        joinDTO.setGender("W");
                    } else {
                        joinDTO.setGender("");
                    }
                }

                if(null != birthday) {
                    // BIRTH(생년월일)
                    String birth = birthyear;
                    if(birthday.contains("-")) {
                        birth = birth + birthday.replace("-", "");
                    }
                    joinDTO.setBirth(birth);
                }

                int insertResult = joinService.insertMemberInfo(joinDTO);
                logger.info("NAVER join result={}", insertResult);
            }

        } catch (Exception e) {
            logger.error("NAVER OAuth 회원가입 중 오류 발생: {}", e.getMessage());
        }

        // Naver는 OIDC가 아니라 OAuth2이므로 DefaultOAuth2User로 반환
        // nameAttributeKey를 "id"로 주고, attributes는 'resp'(언래핑된 맵)로 설정
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                resp,
                "id"
        );
    }
    private String str(Object o) { return o == null ? null : String.valueOf(o); }
}
