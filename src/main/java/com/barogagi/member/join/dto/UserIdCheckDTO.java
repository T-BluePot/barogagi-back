package com.barogagi.member.join.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdCheckDTO extends DefaultVO {
    private String userId = "";
}
