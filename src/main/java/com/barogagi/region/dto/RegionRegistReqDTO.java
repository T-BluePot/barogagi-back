package com.barogagi.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "지역 정보 DTO")
public class RegionRegistReqDTO {
    // 카카오 api에서 지역 데이터 어떻게 넘겨주는지 확인 필요

    @Schema(description = "지역명 대분류", example = "서울특별시")
    public String regionNm1;

    @Schema(description = "지역명 소분류", example = "강남구")
    public String regionNm2;

//    public int regionNum;          // 지역 번호
//    public String regionLevel1;    // 대분류
//    public String regionLevel2;    // 시/군
//    public String regionLevel3;    // 구
//    public String regionLevel4;    // 동/면/리
}
