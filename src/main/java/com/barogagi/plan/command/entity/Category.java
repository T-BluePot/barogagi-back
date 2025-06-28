package com.barogagi.plan.command.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
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
