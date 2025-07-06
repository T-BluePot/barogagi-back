package com.barogagi.member.join.vo;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinVO extends DefaultVO {

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
}
