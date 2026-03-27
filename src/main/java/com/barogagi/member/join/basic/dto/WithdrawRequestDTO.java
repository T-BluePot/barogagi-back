package com.barogagi.member.join.basic.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequestDTO {
    private int reasonNo;
    private String withdrawReason;
}
