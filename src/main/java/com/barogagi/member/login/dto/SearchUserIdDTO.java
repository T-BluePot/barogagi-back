package com.barogagi.member.login.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchUserIdDTO extends DefaultVO {

    private String tel = "";
}