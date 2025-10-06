package com.barogagi.schedule.dto;

import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.query.vo.PlanDetailVO;
import com.barogagi.region.dto.RegionRegistReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Schema(description = "일정 등록 요청 DTO")
public class ScheduleRegistReqDTO {
    private String scheduleNm;      // 일정명
    private String startDate;       // 시작 날짜
    private String endDate;         // 종료 날짜
    private String comment;         // 추가 고려사항

    // 계획 리스트
    private List<PlanRegistReqDTO> planRegistReqDTOList;
}
