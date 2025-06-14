package com.barogagi.approval.vo;

import com.barogagi.config.vo.DefailtVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalCompleteVO extends DefailtVO {
    private String tel = "";
    private String authCode = "";
    private String type = "";
}
