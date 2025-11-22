package com.barogagi.member.info;

import lombok.Getter;

@Getter
public class MemberInfoException extends RuntimeException{
    private final String resultCode;

    public MemberInfoException(String resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
