package com.barogagi.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "태그 정보 리스트 DTO")
public class TagRegistReqDTO {
    @Schema(description = "태그 번호", example = "1")
    public int tagNum;

    @Schema(description = "태그 이름", example = "디저트")
    public String tagNm;
}
