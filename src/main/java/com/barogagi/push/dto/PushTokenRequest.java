package com.barogagi.push.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushTokenRequest {

    private String fcmToken;

    private String deviceType;

    private String appVersion;
}
