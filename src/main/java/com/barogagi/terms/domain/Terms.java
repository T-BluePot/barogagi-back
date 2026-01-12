package com.barogagi.terms.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TERMS")
@Getter
@Setter
public class Terms {

    @Schema(description = "약관 번호", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TERMS_NUM")
    private int termsNum;

    @Schema(description = "제목")
    @Column(name = "TITLE", nullable = false)
    private String title;

    @Schema(description = "내용")
    @Column(name = "CONTENTS", nullable = false)
    private String contents;

    @Schema(description = "약관 타입", example = "JOIN-MEMBERSHIP(회원가입 약관)")
    @Column(name = "TERMS_TYPE", nullable = false)
    private String termsType;

    @Schema(description = "사용 여부", example = "Y:사용 / N:미사용")
    @Column(name = "USE_YN", nullable = false)
    private String useYn;

    @Schema(description = "등록일")
    @Column(name = "REG_DATE", nullable = false)
    private String regDate;

    @Schema(description = "필수 여부", example = "Y:필수 / N:선택")
    @Column(name = "ESSENTIAL_YN", nullable = false)
    private String essentialYn;

    @Schema(description = "정렬 순서")
    @Column(name = "SORT", nullable = false)
    private int sort;
}
