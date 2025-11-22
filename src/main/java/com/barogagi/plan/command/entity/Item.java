package com.barogagi.plan.command.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "ITEM")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_NUM")
    private Integer itemNum;            // 아이템 번호

    @Column(name = "ITEM_NM", nullable = false, length = 100)
    private String itemNm;              // 아이템명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_NUM", nullable = false)
    private Category category;          // 카테고리 번호
}
