package com.barogagi.schedule.dto;

import com.barogagi.region.dto.RegionRegistReqDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagicScheduleReqDTO {
    private String scheduleNm;   // 선택 (없으면 기본값)
    private String startDate;    // 필수 yyyy-MM-dd
    private String endDate;      // 필수 yyyy-MM-dd
    private String startTime;    // 선택 HH:mm
    private String endTime;      // 선택 HH:mm
    private List<RegionRegistReqDTO> scheduleRegionRegistReqDTOList; // 필수
}