package com.barogagi.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인기 장소 응답 DTO")
public class PopularPlaceResDTO {

    @Schema(description = "장소명")
    public String planNm;

    @Schema(description = "장소 주소")
    public String planAddress;

    @Schema(description = "장소 링크")
    public String planLink;

    @Schema(description = "이미지 url")
    public String imageUrl;
}