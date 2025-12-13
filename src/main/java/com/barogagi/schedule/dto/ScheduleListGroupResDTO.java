package com.barogagi.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
@Getter
@ToString
@Builder(toBuilder = true)
@Schema(description = "과거/미래 일정 목록 조회 DTO")
public class ScheduleListGroupResDTO {
    private List<ScheduleListResDTO> pastSchedules;
    private List<ScheduleListResDTO> upcomingSchedules;
}
