package com.barogagi.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoPlaceReqDTO {
    @JsonProperty("address_name")
    String addressName;         // 상세주소

    @JsonProperty("place_name")
    String placeName;           // 장소명
}
