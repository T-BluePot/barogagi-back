package com.barogagi.member.join.oauth.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.basic.dto.JoinRequestDTO;
import com.barogagi.member.join.basic.exception.JoinException;
import com.barogagi.member.join.basic.service.MemberSignupService;
import com.barogagi.member.join.oauth.dto.OAuth2UserDTO;
import com.barogagi.member.repository.DeletedMembershipRepository;
import com.barogagi.member.service.UserMembershipService;
import com.barogagi.util.EncryptUtil;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 1) OIDC 전용 서비스
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {

    private final EncryptUtil encryptUtil;
    private final MemberSignupService memberSignupService;
    private final UserMembershipService userMembershipService;

    private final DeletedMembershipRepository deletedMembershipRepository;

    private static final int REJOIN_BLOCK_DAYS = 90;

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

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

        logger.info("[OIDC] CustomOidcUserService.loadUser called. sub={}, email={}, name={}", sub, email, name);

        try {
            // 일정 기간 동안 동일한 아이디로 회원가입 금지
            LocalDateTime limitDate = LocalDateTime.now().minusDays(REJOIN_BLOCK_DAYS);
            boolean blocked = deletedMembershipRepository.existsRecentlyWithdrawnUser(sub.trim(), limitDate);
            if(blocked) {
                throw new OAuth2AuthenticationException(ErrorCode.FAIL_OAUTH2_LOGIN.getMessage());
            }

            // 구글로 회원가입한 정보가 있는지 체크
            OAuth2UserDTO oAuth2UserDTO = new OAuth2UserDTO();
            oAuth2UserDTO.setSub(sub);
            oAuth2UserDTO.setEmail(encryptUtil.encrypt(email));
            oAuth2UserDTO.setJoinType("GOOGLE");

            // 구글로 회원가입한 정보가 있는지 조회
            UserMembershipInfo userInfo = userMembershipService.findByOAuthSub(oAuth2UserDTO);

            // 없으면 insert
            if (userInfo == null) {
                JoinRequestDTO joinRequestDTO = new JoinRequestDTO();
                joinRequestDTO.setUserId(sub);
                joinRequestDTO.setEmail(encryptUtil.encrypt(email));
                joinRequestDTO.setNickName(name);
                joinRequestDTO.setJoinType("GOOGLE");

                String membershipNo = memberSignupService.signUp(joinRequestDTO);

                logger.info("GOOGLE join membershipNo={}", membershipNo);
            }

        } catch (Exception e) {
            logger.error("GOOGLE OAuth 회원가입 중 오류 발생: {}", e.getMessage());
            throw new OAuth2AuthenticationException(ErrorCode.FAIL_OAUTH2_LOGIN.getMessage());
        }
        return user;
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }
}
