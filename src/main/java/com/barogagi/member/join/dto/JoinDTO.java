package com.barogagi.member.join.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO extends DefaultVO {

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
}
