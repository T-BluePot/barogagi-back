package com.barogagi.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class RegionGeoCodeResDTO {
    @Schema(description = "x 좌표", example = "127.04892851392")
    public String x;

    @Schema(description = "y 좌표", example = "37.5091105328378")
    public String y;

    public int regionNum;

    public String regionLevel1;

    public String regionLevel2;

    public String regionLevel3;

    public String regionLevel4;
}
