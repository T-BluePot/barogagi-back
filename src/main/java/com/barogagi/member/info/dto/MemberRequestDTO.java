package com.barogagi.member.info.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequestDTO {

    // 생년월일
    private String birth = "";

    // 성별
    private String gender = "";

    // 닉네임
    private String nickName = "";
}
