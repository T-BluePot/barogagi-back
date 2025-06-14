package com.barogagi.approval.vo;

import com.barogagi.config.vo.DefailtVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalSendVO extends DefailtVO {
    private String tel = "";
    private String type = "";
}
