package com.barogagi.member.oauth.join.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2UserDTO {
    public String sub = ""; // 구글 고유 ID
    public String email = "";
    public String emailVerified = ""; // 이메일 인증 여부
    public String name = "";
    public String givenName = ""; // 이름
    public String familyName = ""; // 성
    public String picture = ""; // 프로필 사진 URL
    public String locale = ""; // 언어
    public String joinType = ""; // 가입 유형 (예: GOOGLE, NAVER, KAKAO 등)
}
