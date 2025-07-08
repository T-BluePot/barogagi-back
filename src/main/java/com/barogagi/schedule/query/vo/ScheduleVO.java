package com.barogagi.schedule.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ScheduleVO {
    public int scheduleNum;        // 일정 번호 (PK)
    public int membershipNo;       // 회원 번호 (FK)
    public String scheduleNm;      // 일정명
    public String startDate;       // 시작 날짜
    public String endDate;         // 종료 날짜
    public int radius;             // 반경
}
