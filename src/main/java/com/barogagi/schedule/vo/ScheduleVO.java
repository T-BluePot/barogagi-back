package com.barogagi.schedule.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleVO {
    private int scheduleNum;        // 일정 번호 (PK)
    private int membershipNo;       // 회원 번호 (FK)
    private String scheduleNm;      // 일정명
    private String startDate;       // 시작 날짜
    private String endDate;         // 종료 날짜
    private int radius;             // 반경
}
