package com.barogagi.schedule.dto;

import com.barogagi.tag.dto.TagRegistResDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder(toBuilder = true)
@Schema(description = "일정 목록 조회 DTO")
public class ScheduleListResDTO {
    private int scheduleNum;        // 일정 번호 (PK)
    private String scheduleNm;      // 일정명
    private String startDate;       // 시작 날짜
    private String endDate;         // 종료 날짜

    List<TagRegistResDTO> scheduleTagRegistResDTOList;
}
