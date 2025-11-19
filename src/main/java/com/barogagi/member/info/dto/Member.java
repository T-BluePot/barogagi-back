package com.barogagi.member.info.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {

    // 회원번호
    private String membershipNo = "";

    // 아이디
    private String userId = "";

    // 비밀번호
    private String password = "";

    // 이메일 주소
    private String email = "";

    // 생년월일 (YYYYMMDD)
    private String birth = "";

    // 휴대전화번호
    private String tel = "";

    // 성별 (M : 남 / W : 여)
    private String gender = "";

    // 닉네임
    private String nickName = "";

    // 회원가입 종류(BASIC : 기본 / GOOGLE : 구글 / KAKAO : 카카오톡 / NAVER : 네이버)
    private String joinType = "";

    // 프로필 이미지
    private String profileImg = "";

    // 등록일
    private String regDate = "";

    // 수정일
    private String updDate = "";
}
