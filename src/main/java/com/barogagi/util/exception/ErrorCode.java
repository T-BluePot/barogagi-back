package com.barogagi.util.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),

    // Membership
    MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBERSHIP-404", "멤버십 정보가 없습니다."),
    MEMBERSHIP_SERVICE_FAIL(HttpStatus.BAD_GATEWAY, "MEMBERSHIP-502", "멤버십 서비스 호출에 실패했습니다."),

    // Schedule
    SCHEDULE_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "SCH-001", "일정 저장에 실패했습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCH-002", "일정 정보를 찾을 수 없습니다."),
    SCHEDULE_ALREADY_DELETED(HttpStatus.NOT_FOUND, "SCH-003", "이미 삭제된 일정입니다."),

    // Tag
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG-001", "태그 정보를 찾을 수 없습니다."),

    // Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ITEM-001", "아이템 정보를 찾을 수 없습니다."),

    // Region
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION-001", "지역 정보를 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
