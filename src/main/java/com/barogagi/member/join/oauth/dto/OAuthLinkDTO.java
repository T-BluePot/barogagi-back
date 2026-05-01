package com.barogagi.member.join.oauth.dto;

import com.barogagi.member.join.oauth.enums.Environment;
import com.barogagi.member.join.oauth.enums.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLinkDTO {
    private String apiSecretKey;
    private Type type;
    private Environment environment;
}
