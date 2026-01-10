package com.barogagi.ai.dto;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChatMessage {
    private String role;    // "system" | "user" | "assistant"
    private String content; // 메시지 내용
}
