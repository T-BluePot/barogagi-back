package com.barogagi.tag.enums;

import com.barogagi.tag.exception.TagException;
import com.barogagi.util.exception.ErrorCode;

public enum TagType {
    S("일정별 태그"),
    P("계획별 태그");

    private final String description;

    TagType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // DB 저장용 코드 반환
    public String getCode() {
        return this.name();
    }

    /**
     * 코드로부터 Enum 찾기 (예: "S" 또는 "P")
     * @param code
     * @return
     */
    public static TagType fromCode(String code) {
        for (TagType type : TagType.values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TagType code: " + code);
    }

    /**
     * 문자열 값으로부터 Enum 찾기 (예: "S" 또는 "P")
     * @param value
     * @return
     */
    public static TagType fromValue(String value) {
        for (TagType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        throw new TagException(ErrorCode.INVALID_TAG_TYPE);
    }
}
