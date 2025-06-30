package com.barogagi.schedule.dto;

import com.barogagi.plan.query.vo.PlanDetailVO;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@ToString
public class ScheduleDetailResDTO {
    private int scheduleNum;        // 일정 번호 (PK)
    private String scheduleNm;      // 일정명
    private String startDate;       // 시작 날짜
    private String endDate;         // 종료 날짜
    private int radius;             // 반경

    // 계획 리스트
    private List<PlanDetailVO> planDetailVOList;
}
