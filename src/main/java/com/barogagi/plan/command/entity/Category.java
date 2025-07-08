package com.barogagi.plan.command.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "CATEGORY")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_NUM")
    private Integer categoryNum;        // 카테고리 번호

    @Column(name = "CATEGORY_NM", nullable = false, length = 100)
    private String categoryNm;          // 카테고리명
}
