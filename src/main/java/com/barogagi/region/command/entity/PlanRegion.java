package com.barogagi.region.command.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PLAN_REGION")
public class PlanRegion {

    @EmbeddedId
    private PlanRegionNum planRegionNum;    // (복합키) 계획 번호, 지역 번호
}
