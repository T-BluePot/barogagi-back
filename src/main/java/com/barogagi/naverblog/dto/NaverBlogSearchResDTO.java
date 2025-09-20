package com.barogagi.naverblog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "네이버 블로그 검색 결과 DTO")
public class NaverBlogSearchResDTO {
    @JsonProperty("lastBuildDate")
    private String lastBuildDate;

    @JsonProperty("total")
    private int total;

    @JsonProperty("start")
    private int start;

    @JsonProperty("display")
    private int display;

    @JsonProperty("items")
    private List<NaverBlogResDTO> items;
}