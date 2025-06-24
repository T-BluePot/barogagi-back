package com.barogagi.schedule.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlanVO {
    private int planNum;            // 계획 번호 (PK)
    private int categoryNum;        // 카테고리 번호 (FK)
    private String planNm;          // 계획명
    private String startTime;       // 시작시간
    private String endTime;         // 종료시간
    private int regionNum;          // 지역 번호 (FK)
    private int scheduleNum;        // 일정 번호 (FK)
    private int membershipNo;       // 회원 번호 (FK)
}
