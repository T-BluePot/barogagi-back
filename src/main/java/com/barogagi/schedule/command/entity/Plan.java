package com.barogagi.schedule.command.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder(toBuilder = true)
@Table(name = "PLAN")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAN_NUM")
    private int planNum;                // 계획 번호 (PK)

    @Column(name = "CATEGORY_NUM", nullable = false)
    private int categoryNum;            // 카테고리 번호 (FK)

    @Column(name = "PLAN_NM", nullable = false, length = 100)
    private String planNm;              // 계획명

    @Column(name = "START_TIME")
    private String startTime;           // 시작 시간

    @Column(name = "END_TIME")
    private String endTime;             // 종료 시간

    @Column(name = "REGION_NUM", nullable = false)
    private int regionNum;              // 지역 번호 (FK)

    @Column(name = "SCHEDULE_NUM", nullable = false)
    private int scheduleNum;            // 일정 번호 (FK)

    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private int membershipNo;           // 회원 번호 (FK)

}
