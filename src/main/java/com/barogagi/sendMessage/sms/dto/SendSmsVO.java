package com.barogagi.sendMessage.sms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsVO {
    private String recipientTel = "";
    private String messageContent = "";
}
