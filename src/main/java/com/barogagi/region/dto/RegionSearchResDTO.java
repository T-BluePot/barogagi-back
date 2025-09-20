package com.barogagi.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class RegionSearchResDTO {
    public String regionNm;
    public Integer regionNum;
}
