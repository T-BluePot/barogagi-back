package com.barogagi.member.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.join.oauth.dto.OAuth2UserDTO;

public interface UserMembershipService {

    // 특정 oauth로 회원가입한 정보가 있는지 체크
    UserMembershipInfo findByOAuthSub(OAuth2UserDTO oAuth2UserDTO);
}
