package com.barogagi.plan.dto;

import com.barogagi.kakaoplace.dto.KakaoPlaceResDTO;
import com.barogagi.region.dto.RegionRegistReqDTO;
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
@Schema(description = "계획 등록 응답 DTO")
public class PlanRegistResDTO {

    @Schema(description = "시작 시간", example = "08:00")
    public String startTime;

    @Schema(description = "종료 시간", example = "09:00")
    public String endTime;

    @Schema(description = "아이템 번호", example = "1")
    public int itemNum;

    @Schema(description = "아이템 명", example = "1")
    public String itemNm;

    @Schema(description = "카테고리 번호", example = "1")
    public int categoryNum;

    @Schema(description = "카테고리 명", example = "1")
    public String categoryNm;

    @Schema(description = "장소 번호")
    public Integer planNum;

    @Schema(description = "장소명")
    public String planNm;

    @Schema(description = "장소 링크(이미지 불러오기용)")
    public String planLink;

    @Schema(description = "장소 한줄 설명(ai 생성)")
    public String planDescription;

    @Schema(description = "장소 주소")
    public String planAddress;

    @Schema(description = "지역 정보 DTO")
    public String regionNm;

    @Schema(description = "지역 번호")
    public Integer regionNum;

    @Schema(description = "태그 목록")
    public List<TagRegistResDTO> tagRegistResDTOList;

    // private KakaoPlaceResDTO aiChosen;
}

