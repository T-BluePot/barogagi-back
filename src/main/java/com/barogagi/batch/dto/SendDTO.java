package com.barogagi.batch.dto;

import com.barogagi.batch.enums.Channel;
import com.barogagi.sendMessage.email.dto.SendMailDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SendDTO {
    private Map<String, String> variables;  // 문자 내용에 필요한 데이터
    private String tel;
    private String templateId;
    private SendMailDTO sendMailDTO;
}
