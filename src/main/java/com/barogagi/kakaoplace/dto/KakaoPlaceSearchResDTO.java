package com.barogagi.kakaoplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "카카오 장소 추천 목록 결과 DTO")
public class KakaoPlaceSearchResDTO {
    @JsonProperty("documents")
    private List<KakaoPlaceResDTO> documents;
}