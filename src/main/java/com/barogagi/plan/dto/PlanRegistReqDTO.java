package com.barogagi.plan.dto;

import com.barogagi.region.dto.RegionRegistReqDTO;
import com.barogagi.tag.dto.TagRegistReqDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.Getter;
import lombok.ToString;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@ToString
@Schema(description = "계획 등록 요청 DTO")
public class PlanRegistReqDTO {

    //@Schema(description = "계획 이름", example = "프랜차이즈카페")
    //public String planNm;

    @Schema(description = "시작 시간", example = "08:00")
    public String startTime;

    @Schema(description = "종료 시간", example = "09:00")
    public String endTime;

    @Schema(description = "아이템 번호", example = "1")
    public int itemNum;

    @Schema(description = "카테고리 번호", example = "1")
    public int categoryNum;

    @Schema(description = "지역 정보 DTO")
    public List<RegionRegistReqDTO> regionRegistReqDTOList;

    @Schema(description = "태그 목록")
    public List<TagRegistReqDTO> tagRegistReqDTOList;
}

