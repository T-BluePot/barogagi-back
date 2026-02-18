package com.barogagi.member.join.basic.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequestDTO {

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
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 닉네임
    private String nickName = "";

    // 회원가입 종류
    private String joinType = "BASIC";
}
