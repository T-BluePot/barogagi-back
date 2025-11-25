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

    @Schema(description = "계획 태그 목록")
    public List<TagRegistReqDTO> planTagRegistReqDTOList;

    // 사용자가 직접 세부일정을 추가한 경우에만 필요한 값
    @Schema(description = "사용자가 수동으로 추가한 일정인지 여부(AI 생성 안함)", example = "Y")
    public String isUserAdded;

    @Schema(description = "사용자 직접 추가 CASE 1. 카카오 API 장소검색으로 추가한 장소 정보.")
    UserAddedPlaceDTO userAddedPlaceDTO;

    @Schema(description = "사용자 직접 추가 CASE 2. 사용자가 직접 입력한 장소", example = "친구집 방문")
    public String planNm;




}

