package com.barogagi.member.login.vo;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO extends DefaultVO {
    private String userId = "";
    private String password = "";
}
