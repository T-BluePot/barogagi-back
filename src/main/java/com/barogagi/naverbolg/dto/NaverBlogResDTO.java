package com.barogagi.naverbolg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "네이버 블로그 검색 결과 DTO")
public class NaverBlogResDTO {
    @JsonProperty("title")
    private String title;

    @JsonProperty("link")
    private String link;

    @JsonProperty("description")
    private String description;

    @JsonProperty("bloggername")
    private String bloggerName;

    @JsonProperty("bloggerlink")
    private String bloggerLink;

    @JsonProperty("postdate")
    private String postDate;

}