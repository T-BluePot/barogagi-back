package com.barogagi.schedule.dto;

import com.barogagi.plan.dto.PlanRegistReqDTO;
import com.barogagi.plan.dto.PlanRegistResDTO;
import com.barogagi.tag.dto.TagRegistReqDTO;
import com.barogagi.tag.dto.TagRegistResDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder(toBuilder = true)
@Schema(description = "일정 등록 응답 DTO")
public class ScheduleRegistResDTO {
    private Integer scheduleNum;    // 일정 번호
    private String scheduleNm;      // 일정명
    private String startDate;       // 시작 날짜
    private String endDate;         // 종료 날짜

    // 일정 태그 목록 (스케쥴 태그)
    public List<TagRegistResDTO> scheduleTagRegistResDTOList;

    // 계획 리스트
    private List<PlanRegistResDTO> planRegistResDTOList;
}
