package com.barogagi.member.join.oauth.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.basic.dto.Gender;
import com.barogagi.member.join.basic.dto.JoinRequestDTO;
import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.join.basic.service.MemberSignupService;
import com.barogagi.member.join.oauth.dto.OAuth2UserDTO;
import com.barogagi.member.service.UserMembershipService;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    private final EncryptUtil encryptUtil;
    private final MemberSignupService memberSignupService;
    private final UserMembershipService userMembershipService;

    private static final Logger logger = LoggerFactory.getLogger(NaverOAuth2UserService.class);

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) {

        OAuth2User user = super.loadUser(req);

        // provider 구분 (여기서는 naver일 때만 처리)
        String registrationId = req.getClientRegistration().getRegistrationId();
        if (!"naver".equals(registrationId)) {
            return user; // 다른 provider는 건드리지 않음
        }

        Map<String, Object> attrs = user.getAttributes();
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = (Map<String, Object>) attrs.get("response");

        if (resp == null) {
            throw new JoinException(ErrorCode.FAIL_SIGN_UP);
        }

        String id = str(resp.get("id"));
        String nickName = str(resp.get("nickname"));
        String gender = str(resp.get("gender"));
        String email = str(resp.get("email"));
        String birthday = str(resp.get("birthday"));
        String birthyear = str(resp.get("birthyear"));
        String tel = str(resp.get("mobile"));

        try {
            // 네이버로 회원가입한 정보가 있는지 체크
            OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
            oAuth2UserDTO.setSub(id);
            oAuth2UserDTO.setEmail(encryptUtil.encrypt(email));
            oAuth2UserDTO.setJoinType("NAVER");

            // 네이버로 회원가입한 정보가 있는지 조회
            UserMembershipInfo userInfo = userMembershipService.findByOAuthSub(oAuth2UserDTO);

            // 없으면 insert
            if (userInfo == null) {
                JoinRequestDTO joinRequestDTO = new JoinRequestDTO();
                joinRequestDTO.setUserId(id);
                joinRequestDTO.setEmail(encryptUtil.encrypt(email));
                joinRequestDTO.setNickName(nickName);
                joinRequestDTO.setJoinType("NAVER");

                // gender(성별) : M(남성), F(여성)
                if(null != gender) {
                    if(gender.equals("M")) {  // 남성
                        joinRequestDTO.setGender(Gender.M);
                    } else if(gender.equals("F")) {  // 여성
                        joinRequestDTO.setGender(Gender.W);
                    }
                }

                if(null != birthday && null != birthyear) {
                    // BIRTH(생년월일) (출생연도 + 생일)
                    String birth = birthyear;
                    if(birthday.contains("-")) {
                        birth = birth + birthday.replace("-", "");
                    }
                    joinRequestDTO.setBirth(birth);
                }

                if(null != tel) {
                    // 휴대 전화번호
                    joinRequestDTO.setTel(encryptUtil.encrypt(tel.replaceAll("[^0-9]", "")));
                }

                String membershipNo = memberSignupService.signUp(joinRequestDTO);
                logger.info("NAVER join membershipNo={}", membershipNo);
            }

        } catch (Exception e) {
            logger.error("NAVER OAuth 회원가입 중 오류 발생: {}", e.getMessage());
        }

        // Naver는 OIDC가 아니라 OAuth2이므로 DefaultOAuth2User로 반환
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                resp,
                "id"
        );
    }
    private String str(Object o) { return o == null ? null : String.valueOf(o); }
}
