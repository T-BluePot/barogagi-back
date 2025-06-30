package com.barogagi.region.command.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder(toBuilder = true)
@Table(name = "PLAN_REGION")
public class PlanRegion {

    @EmbeddedId
    private PlanRegionNum planRegionNum;    // (복합키) 계획 번호, 지역 번호
}
