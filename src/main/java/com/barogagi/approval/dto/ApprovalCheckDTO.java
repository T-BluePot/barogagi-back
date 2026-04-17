package com.barogagi.approval.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApprovalCheckDTO {
    private String tel;
    private String completeYn;
    private String type;
    private LocalDateTime regDate;
}
