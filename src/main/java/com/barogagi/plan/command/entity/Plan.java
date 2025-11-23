package com.barogagi.plan.command.entity;

import com.barogagi.plan.command.ex_entity.PlanUserMembershipInfo;
import com.barogagi.region.command.entity.Place;
import com.barogagi.region.command.entity.PlanRegion;
import com.barogagi.schedule.command.entity.Schedule;
import com.barogagi.tag.command.entity.PlanTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "PLAN_LINK")
    public String planLink;             // 장소 링크 (이미지 불러오기용)

    @Column(name = "PLAN_DESCRIPTION")
    private String planDescription;     // 장소 한줄 설명(AI 생성)

    @Column(name = "PLAN_ADDRESS")
    private String planAddress;         // 장소 주소

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_NUM", nullable = false)
    private Schedule schedule;          // 일정 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBERSHIP_NO", nullable = false)
    private PlanUserMembershipInfo user;   // 회원 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_NUM", nullable = false)
    private Item item;                  // 아이템 번호

    // PLACE와 1:1 mapping
    @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Place place;

    @CreationTimestamp
    @Column(name = "REG_DATE", updatable = false)
    private LocalDateTime regDate;      // 등록일

    @UpdateTimestamp
    @Column(name = "UPD_DATE")
    private LocalDateTime updDate;      // 수정일 (업데이트 시 자동 변경)

    @Column(name = "DEL_YN", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn;               // 삭제 여부(Y: 삭제, N: 미삭제)

    @OneToMany(mappedBy = "plan", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PlanRegion> planRegions = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PlanTag> planTags = new ArrayList<>();


}
