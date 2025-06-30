package com.barogagi.region.command.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class PlanRegionNum {
    // 계획별 지역 복합키

    @Column(name="REGION_NUM")
    private Integer regionNum;      // 지역 번호

    @Column(name="PLAN_NUM")
    private Integer PlanNum;        // 계획 변호
}
