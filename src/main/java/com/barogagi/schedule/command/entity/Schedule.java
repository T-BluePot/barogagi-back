package com.barogagi.schedule.command.entity;

import com.barogagi.plan.command.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "SCHEDULE")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_NUM")
    private int scheduleNum;            // 일정 번호 (PK)

    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private int membershipNo;           // 회원 번호 (FK)

    @Column(name = "SCHEDULE_NM", nullable = false, length = 100)
    private String scheduleNm;          // 일정명

    @Column(name = "START_DATE", nullable = false)
    private String startDate;           // 시작 날짜

    @Column(name = "END_DATE", nullable = false)
    private String endDate;             // 종료 날짜

    @Column(name = "RADIUS", nullable = false)
    private int radius;                 // 추천 반경 (미터 단위)

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> plans = new ArrayList<>();
}