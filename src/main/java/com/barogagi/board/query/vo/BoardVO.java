package com.barogagi.board.query.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공지사항 VO")
public class BoardVO {

    @Schema(description = "공지사항 번호")
    private Integer boardNum;

    @Schema(description = "제목")
    private String boardTitle;

    @Schema(description = "내용")
    private String boardContent;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "중요 공지 여부", example = "N")
    private String isImportant;

    @Schema(description = "등록일")
    private LocalDateTime regDate;
}