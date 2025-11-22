package com.barogagi.member.login.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginVO extends DefaultVO {

    private String userId = "";
    private String password = "";
    private String tel = "";

    private String membershipNo = "";
}