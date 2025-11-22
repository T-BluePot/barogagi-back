package com.barogagi.member.oauth.join.mapper;

import com.barogagi.member.oauth.join.dto.OAuth2UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2UserMapper {
    // 구글로 회원가입한 정보가 있는지 체크
    OAuth2UserDTO findByOAuthSub(OAuth2UserDTO oAuth2UserDTO);
}
