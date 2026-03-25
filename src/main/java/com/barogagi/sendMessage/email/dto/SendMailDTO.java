package com.barogagi.sendMessage.email.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SendMailDTO {
    // 발신자
    private String from;

    // 수신자
    private String to;

    // 메일 제목
    private String subject;

    // 메일 내용
    private String body;
}
