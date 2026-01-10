package com.barogagi.tag.enums;

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

    // 코드로부터 Enum 찾기
    public static TagType fromCode(String code) {
        for (TagType type : TagType.values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TagType code: " + code);
    }
}
