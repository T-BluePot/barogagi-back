package com.barogagi.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "지역 정보 DTO")
@Builder(toBuilder = true)
public class RegionRegistReqDTO {
//    @Schema(description = "지역명 대분류", example = "서울특별시")
//    public String regionNm1;

//    @Schema(description = "지역명 소분류", example = "강남구")
//    public String regionNm2;

//    @Schema(description = "x 좌표", example = "127.04892851392")
//    public String x;

//    @Schema(description = "y 좌표", example = "37.5091105328378")
//    public String y;

    public int regionNum;          // 지역 번호
    public String regionLevel1;    // 대분류
    public String regionLevel2;    // 시/군
    public String regionLevel3;    // 구
    public String regionLevel4;    // 동/면/리
}
