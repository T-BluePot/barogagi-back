package com.barogagi.sendSms.dto;

import com.barogagi.config.vo.DefailtVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsVO extends DefailtVO {
    private String recipientTel = "";
    private String messageContent = "";
}
