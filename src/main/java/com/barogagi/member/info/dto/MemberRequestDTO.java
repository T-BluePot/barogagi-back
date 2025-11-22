package com.barogagi.member.info.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequestDTO extends DefaultVO {

    // 비밀번호
    private String password = "";

    // 이메일
    private String email = "";

    // 생년월일
    private String birth = "";

    // 성별
    private String gender = "";

    // 닉네임
    private String nickName = "";

    // 프로필 이미지
    private String profileImg = "";

    // 휴대폰 번호
    private String tel = "";
}
