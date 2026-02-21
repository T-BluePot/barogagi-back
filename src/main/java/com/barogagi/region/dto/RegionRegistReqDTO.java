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
    public int regionNum;          // 지역 번호
    public String regionLevel1;    // 대분류
    public String regionLevel2;    // 시/군
    public String regionLevel3;    // 구
    public String regionLevel4;    // 동/면/리
}
