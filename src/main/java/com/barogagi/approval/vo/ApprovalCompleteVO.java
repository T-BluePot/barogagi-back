package com.barogagi.approval.vo;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalCompleteVO extends DefaultVO {
    private String tel = "";
    private String authCode = "";
    private String type = "";
}
