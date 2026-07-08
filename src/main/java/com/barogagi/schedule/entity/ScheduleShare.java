package com.barogagi.schedule.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_SHARE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScheduleShare {

    @Schema(description = "번호")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHARE_NUM", nullable = false)
    private Long shareNum;

    @Schema(description = "일정 번호")
    @Column(name = "SCHEDULE_NUM", nullable = false)
    private int scheduleNum;

    @Schema(description = "공유 토큰")
    @Column(name = "SHARE_TOKEN", nullable = false)
    private String shareToken;

    @Schema(description = "회원번호")
    @Column(name = "MEMBERSHIP_NO", nullable = false)
    private String membershipNo;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "EXPIRE_AT")
    private LocalDateTime expireAt;
}
