package com.barogagi.sendMessage.alimTalk.dto;

import lombok.Getter;

@Getter
public class PurpleBookResponse {

    private String resultCode;
    private String resultMessage;

    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(resultCode);
    }
}
