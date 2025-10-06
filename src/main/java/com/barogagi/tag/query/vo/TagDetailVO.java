package com.barogagi.tag.query.vo;

import com.barogagi.tag.enums.TagType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TagDetailVO {
    private int tagNum;             // 태그 번호
    private String tagNm;           // 태그명
    private TagType tagType;        // 태그 타입
    private int categoryNum;        // 카테고리 번호 (일정(스타일) 태그는 null, 계획(plan) 태그는 카테고리 번호)
}
