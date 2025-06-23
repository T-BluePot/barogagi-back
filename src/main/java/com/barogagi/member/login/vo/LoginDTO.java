package com.barogagi.member.login.vo;

import com.barogagi.config.vo.DefailtVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO extends DefailtVO {
    private String userId = "";
    private String password = "";
}
