package com.barogagi.tag.command.entity;

import com.barogagi.plan.command.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PLAN_TAG")
public class PlanTag {

    @EmbeddedId
    private PlanTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planNum")
    @JoinColumn(name = "PLAN_NUM")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagNum")
    @JoinColumn(name = "TAG_NUM")
    private Tag tag;
}

