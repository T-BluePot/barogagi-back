package com.barogagi.member.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "DELETED_MEMBERSHIP_INFO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeletedMembershipInfo {

    @Schema(description = "회원번호")
    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private String membershipNo;

    @Schema(description = "아이디")
    @Id
    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Schema(description = "가입일")
    @Column(name = "JOINED_AT", nullable = false)
    private LocalDateTime joinedAt;

    @Schema(description = "탈퇴일")
    @Column(name = "WITHDRAWN_AT", nullable = false)
    private LocalDateTime withdrawnAt;

    @Schema(description = "탈퇴 사유 번호")
    @Column(name = "REASON_NO", nullable = false)
    private int reasonNo;

    @Schema(description = "탈퇴 사유")
    @Column(name = "WITHDRAW_REASON")
    private String withdrawReason;
}
