package com.barogagi.approval.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalVO {

    // 전화번호
    private String tel = "";

    // 인증번호
    private String authCode = "";

    // 완료 여부(Y:완료/N:미완료)
    private String completeYn = "";

    // 타입
    private String type = "";

    // 메시지 내용
    private String messageContent = "";
}
