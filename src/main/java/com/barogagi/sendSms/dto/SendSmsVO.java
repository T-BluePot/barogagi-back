package com.barogagi.sendSms.dto;

import com.barogagi.config.vo.DefaultVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsVO extends DefaultVO {
    private String recipientTel = "";
    private String messageContent = "";
}
