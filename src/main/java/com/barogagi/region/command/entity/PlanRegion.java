package com.barogagi.region.command.entity;

import com.barogagi.plan.command.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PLAN_REGION")
public class PlanRegion {

    @EmbeddedId
    private PlanRegionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planNum")
    @JoinColumn(name = "PLAN_NUM")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("regionNum")
    @JoinColumn(name = "REGION_NUM")
    private Region region;
}
