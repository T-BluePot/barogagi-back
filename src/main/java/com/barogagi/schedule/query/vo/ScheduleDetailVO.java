package com.barogagi.schedule.query.vo;

import com.barogagi.plan.query.vo.PlanDetailVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ScheduleDetailVO {
    public int scheduleNum;        // 일정 번호 (PK)
    public String scheduleNm;      // 일정명
    public String startDate;       // 시작 날짜
    public String endDate;         // 종료 날짜
    public int radius;             // 반경
}
