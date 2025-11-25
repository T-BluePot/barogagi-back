package com.barogagi.tag.dto;

import com.barogagi.tag.enums.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

public class TagSearchReqDTO {
    @Schema(description = "태그 타입 (S는 스타일 태그, P는 세부 일정 태그)", example = "P")
    public TagType tagType;

    @Schema(description = "카테고리 번호 (스타일 태그인 경우 null)", example = "1")
    public Integer categoryNum;
}