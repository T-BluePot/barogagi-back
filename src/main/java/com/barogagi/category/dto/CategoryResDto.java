package com.barogagi.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(toBuilder = true)
@Schema(description = "카테고리 목록 조회 DTO")
public class CategoryResDto {
    @Schema(description = "카테고리 번호", example = "1")
    public int categoryNum;

    @Schema(description = "카테고리 명", example = "식사")
    public String categoryNm;
}
