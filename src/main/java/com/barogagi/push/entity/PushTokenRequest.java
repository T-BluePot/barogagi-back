package com.barogagi.push.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushTokenRequest {

    private String fcmToken;

    private String deviceType;

    private String appVersion;
}
