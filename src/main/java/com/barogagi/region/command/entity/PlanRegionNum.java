package com.barogagi.region.command.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
public class PlanRegionNum {
    // 계획별 지역 복합키

    @Column(name="REGION_NUM")
    private Integer regionNum;      // 지역 번호

    @Column(name="PLAN_NUM")
    private Integer PlanNum;        // 계획 변호
}
