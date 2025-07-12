package com.barogagi.plan.command.entity;

import com.barogagi.plan.command.ex_entity.PlanUserMembershipInfo;
import com.barogagi.schedule.command.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PLAN")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAN_NUM")
    private Integer planNum;            // 계획 번호 (PK)

    @Column(name = "PLAN_NM", nullable = false, length = 100)
    private String planNm;              // 계획명

    @Column(name = "START_TIME")
    private String startTime;           // 시작 시간

    @Column(name = "END_TIME")
    private String endTime;             // 종료 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_NUM", nullable = false)
    private Schedule schedule;          // 일정 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBERSHIP_NO", nullable = false)
    private PlanUserMembershipInfo user;   // 회원 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_NUM", nullable = false)
    private Item item;                  // 아이템 번호
}
