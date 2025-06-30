package com.barogagi.plan.query.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PlanVO {
    public int planNum;            // 계획 번호 (PK)
    public int categoryNum;        // 카테고리 번호 (FK)
    public String planNm;          // 계획명
    public String startTime;       // 시작시간
    public String endTime;         // 종료시간
    public int regionNum;          // 지역 번호 (FK)
    public int scheduleNum;        // 일정 번호 (FK)
    public int membershipNo;       // 회원 번호 (FK)
}
