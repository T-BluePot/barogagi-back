package com.barogagi.region.command.entity;

import com.barogagi.plan.command.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PLACE")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLACE_NUM")
    private Integer placeNum;

    @Column(name = "REGION_NM", nullable = false)
    private String regionNm;

    @Column(name = "ADDRESS")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_NUM")
    private Region region;

    @Column(name = "PLAN_LINK")
    private String planLink;

    @Column(name = "PLACE_DESCRIPTION")
    private String placeDescription;

    // PLAN과 1:1 mapping
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAN_NUM", unique = true)
    private Plan plan;
}
