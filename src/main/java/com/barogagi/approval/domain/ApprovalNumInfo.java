package com.barogagi.approval.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "APPROVAL_NUM_INFO")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalNumInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "APPROVAL_NUM", nullable = false)
    private int approvalNum;

    @Schema(description = "전화번호", example = "01012345678")
    @Column(name = "TEL", nullable = false)
    private String tel;

    @Schema(description = "인증번호", example = "123456")
    @Column(name = "AUTH_CODE", nullable = false)
    private String authCode;

    @Schema(description = "완료 여부", example = "Y:완료 / N:미완료 / C:삭제")
    @Column(name = "COMPLETE_YN", nullable = false)
    private String completeYn;

    @Schema(description = "문자내용")
    @Column(name = "MESSAGE_CONTENT", nullable = false)
    private String messageContent;

    @Schema(description = "타입", example = "JOIN-MEMBERSHIP(회원가입용)")
    @Column(name = "TYPE", nullable = false)
    private String type;

    @Schema(description = "등록일")
    @Column(name = "REG_DATE")
    private LocalDateTime regDate;

    @Schema(description = "완료일")
    @Column(name = "COMPLETE_DATE")
    private LocalDateTime completeDate;

    @Schema(description = "취소일")
    @Column(name = "CANCEL_DATE")
    private LocalDateTime cancelDate;
}
