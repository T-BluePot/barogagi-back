package com.barogagi.kakaoplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "카카오 장소 추천 목록 DTO")
public class KakaoPlaceResDTO {

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("category_group_name")
    private String categoryGroupName;

    @JsonProperty("distance")
    private String distance;

    @JsonProperty("id")
    private String id;

    @JsonProperty("x")
    private String x;

    @JsonProperty("y")
    private String y;

    @JsonProperty("place_url")
    private String placeUrl;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    @JsonProperty("phone")
    private String phone;
}
