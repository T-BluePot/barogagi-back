package com.barogagi.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(toBuilder = true)
@Schema(description = "아이템(세부 카테고리) 목록 조회 DTO")
public class ItemResDto {
    @Schema(description = "아이템 번호", example = "2")
    public int itemNum;

    @Schema(description = "아이템 명", example = "한식")
    public String itemNm;
}
