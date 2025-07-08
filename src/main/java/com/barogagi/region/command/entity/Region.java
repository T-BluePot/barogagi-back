package com.barogagi.region.command.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "REGION")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REGION_NUM")
    private Integer regionNum;          // 지역 번호

    @Column(name = "REGION_LEVEL_1")
    private String regionLevel1;        // 대분류

    @Column(name = "REGION_LEVEL_2")
    private String regionLevel2;        // 시/군

    @Column(name = "REGION_LEVEL_3")
    private String regionLevel3;        // 구

    @Column(name = "REGION_LEVEL_4")
    private String regionLevel4;        // 동/면/리
}
