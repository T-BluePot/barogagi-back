package com.barogagi.kakaoplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "카카오 주소로 좌표 변환 DTO")
public class KakaoGeoCodeResDTO {
    @JsonProperty("x")
    private String x;

    @JsonProperty("y")
    private String y;
}
