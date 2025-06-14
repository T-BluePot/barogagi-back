package com.barogagi.member.login.vo;

import com.barogagi.config.vo.DefailtVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginVO extends DefailtVO {

    private String userId = "";
    private String password = "";
    private String tel = "";

    private String membershipNo = "";
}
