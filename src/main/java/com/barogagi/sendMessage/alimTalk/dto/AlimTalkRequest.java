package com.barogagi.sendMessage.alimTalk.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AlimTalkRequest {

    private String apiKey;  // 퍼플북 발급 키
    private String templateCode;  // 템플릿 코드
    private String phone;  // 수신자 번호
    private Map<String, String> variables;
}
