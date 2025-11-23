package com.barogagi.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "계획에 사용자가 수동으로 추가한 장소 정보 DTO")
public class UserAddedPlaceDTO {
    private String placeName;
    private String placeUrl;
    private String addressName;
}