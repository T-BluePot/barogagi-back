package com.barogagi.member.withdraw.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "WITHDRAW_REASON_CODE")
@Getter
@AllArgsConstructor
public class WithdrawReasonCode {

    @Schema(description = "코드 번호", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REASON_NO", nullable = false)
    private int reasonNo;

    @Schema(description = "사유 내용", example = "서비스 이용이 불편해요")
    @Column(name = "REASON_NM", nullable = false)
    private String reasonNm;

    @Schema(description = "사유 입력 필수 여부", example = "Y/N")
    @Column(name = "ESSENTIAL_YN", nullable = false)
    private String essentialYn;

    @Schema(description = "사용 여부", example = "Y/N")
    @Column(name = "USE_AT", nullable = false)
    private String useAt;

    @Schema(description = "정렬", example = "1")
    @Column(name = "SORT", nullable = false)
    private int sort;
}
