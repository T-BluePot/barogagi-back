package com.barogagi.util.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // API_SECRET_KEY 일치 X
    NOT_EQUAL_API_SECRET_KEY(HttpStatus.UNAUTHORIZED, "A100", "잘못된 접근입니다.", true),

    // ACCESS TOKEN
    NOT_EXIST_ACCESS_AUTH(HttpStatus.UNAUTHORIZED, "A401", "접근 권한이 존재하지 않습니다.", false),
    EXIST_ACCESS_AUTH(HttpStatus.OK, "A200", "회원 번호가 존재합니다.", false),
    EXPIRE_TOKEN(HttpStatus.UNAUTHORIZED, "A300", "Token이 만료되었습니다.", false),

    // Common
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다.", false),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다.", false),
    EMPTY_DATA(HttpStatus.BAD_REQUEST, "C101", "정보를 입력해주세요.", false),

    // Schedule
    SCHEDULE_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "일정 저장에 실패했습니다.", false),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "S002", "일정 정보를 찾을 수 없습니다.", false),
    SCHEDULE_ALREADY_DELETED(HttpStatus.NOT_FOUND, "S003", "이미 삭제된 일정입니다.", false),


    // Region
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "R101", "지역 정보를 찾을 수 없습니다.", false),
    REGION_GEOCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "R102", "지역의 Geocode를 찾을 수 없습니다.", false),

    // Nickname
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "N102", "적합하지 않는 닉네임입니다.", false),
    UNAVAILABLE_NICKNAME(HttpStatus.CONFLICT, "N103", "해당 닉네임 사용이 불가능합니다.", false),
    AVAILABLE_NICKNAME(HttpStatus.OK, "N200", "사용 가능한 닉네임입니다.", false),

    // UserId
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "U102", "적합한 아이디가 아닙니다.", false),
    UNAVAILABLE_USER_ID(HttpStatus.CONFLICT, "U300", "해당 아이디 사용이 불가능합니다.", false),
    AVAILABLE_USER_ID(HttpStatus.OK, "U200", "해당 아이디 사용이 가능합니다.", false),
    NOT_FOUND_USER_ID(HttpStatus.NOT_FOUND, "U400", "해당 아이디의 정보가 존재하지 않습니다.", false),

    // SignUp
    INVALID_SIGN_UP(HttpStatus.BAD_REQUEST, "S102", "적합한 아이디, 비밀번호, 닉네임이 아닙니다.", false),
    SUCCESS_SIGN_UP(HttpStatus.CREATED, "S200", "회원가입에 성공하였습니다.", false),
    FAIL_SIGN_UP(HttpStatus.INTERNAL_SERVER_ERROR, "S300", "회원가입에 실패하였습니다.", false),
    FAIL_DUPLICATE_PHONE_NUMBER(HttpStatus.INTERNAL_SERVER_ERROR, "S400", "동일한 전화번호로 중복 회원가입이 불가능합니다.", false),
    FAIL_INVALID_BIRTH_DATE_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "S401", "올바른 생년월일 형식이 아닙니다.", false),

    // withdrawMember
    SUCCESS_DELETE_ACCOUNT(HttpStatus.OK, "D200", "회원 탈퇴되었습니다.", false),
    FAIL_DELETE_ACCOUNT(HttpStatus.INTERNAL_SERVER_ERROR, "D300", "회원 탈퇴 실패하였습니다.", false),

    // FindUser
    FOUND_ACCOUNT(HttpStatus.OK, "F200", "해당 전화번호로 가입된 아이디가 존재합니다.", false),
    NOT_FOUND_ACCOUNT(HttpStatus.NOT_FOUND, "F201", "해당 전화번호로 가입된 계정이 존재하지 않습니다.", false),

    // UpdatePassword
    SUCCESS_UPDATE_PASSWORD(HttpStatus.OK, "U200", "비밀번호 재설정에 성공하였습니다.", false),
    FAIL_UPDATE_PASSWORD(HttpStatus.INTERNAL_SERVER_ERROR, "U300", "비밀번호 재설정에 실패하였습니다.", false),

    // Login
    NOT_FOUND_USER_INFO(HttpStatus.NOT_FOUND, "L102", "회원 정보가 존재하지 않습니다.", false),
    FAIL_LOGIN(HttpStatus.UNAUTHORIZED, "L103", "로그인에 실패하였습니다.", false),
    SUCCESS_LOGIN(HttpStatus.OK, "L200", "로그인에 성공하였습니다.", false),

    // RefreshToken
    REQUIRED_LOGIN(HttpStatus.UNAUTHORIZED, "R110", "로그인을 진행해주세요.", false),
    REQUIRED_RE_LOGIN(HttpStatus.UNAUTHORIZED, "R120", "로그인을 다시 진행해주세요.", false),
    SUCCESS_REFRESH_TOKEN(HttpStatus.OK, "R200", "토큰이 발급되었습니다.", false),
    FAIL_REFRESH_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "R130", "토큰 발급에 실패하였습니다.", false),
    UNAVAILABLE_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "R301", "유효하지 않은 refresh token입니다.", false),
    NOT_FOUND_AVAILABLE_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "R302", "유효한 token 정보를 찾을 수 없습니다.", false),

    // Logout
    FAIL_LOGOUT(HttpStatus.INTERNAL_SERVER_ERROR, "L300", "로그아웃 실패하였습니다.", false),
    SUCCESS_LOGOUT(HttpStatus.OK, "L200", "로그아웃 되었습니다.", false),

    // Terms
    FOUND_TERMS(HttpStatus.OK, "T200", "약관 조회에 성공하였습니다.", false),
    NOT_FOUND_TERMS(HttpStatus.NOT_FOUND, "T102", "약관이 존재하지 않습니다.", false),
    FAIL_INVALID_TERMS(HttpStatus.INTERNAL_SERVER_ERROR, "T401", "유효한 약관이 아닙니다.", false),
    SUCCESS_INSERT_TERMS(HttpStatus.CREATED, "T200", "약관 저장에 성공하였습니다.", false),
    FAIL_INSERT_TERMS(HttpStatus.INTERNAL_SERVER_ERROR, "T300", "약관 저장에 실패하였습니다.", false),
    FAIL_REQUIRED_TERMS_NOT_AGREED(HttpStatus.INTERNAL_SERVER_ERROR, "T400", "필수 약관 항목에 동의해주셔야 서비스 이용이 가능합니다.", false),

    // MemberInfo
    FOUND_USER_INFO(HttpStatus.OK, "M200", "회원 정보 조회가 완료되었습니다.", false),
    FAIL_UPDATE_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "M404", "사용자 정보 수정 실패하였습니다.", false),
    SUCCESS_UPDATE_USER_INFO(HttpStatus.OK, "M200", "사용자 정보 수정 완료하였습니다.", false),
    FAIL_INVALID_GENDER(HttpStatus.INTERNAL_SERVER_ERROR, "M401", "성별 데이터가 올바르지 않습니다.", false),

    // MainPage
    NOT_FOUND_SCHEDULE(HttpStatus.NOT_FOUND, "M201", "일정이 존재하지 않습니다.", false),
    FOUND_SCHEDULE(HttpStatus.OK, "M200", "조회 성공하였습니다.", false),
    NOT_FOUND_POPULAR_TAG(HttpStatus.NOT_FOUND, "M201", "인기 태그 목록이 존재하지 않습니다.", false),
    FOUND_POPULAR_TAG(HttpStatus.OK, "M200", "인기 태그 조회 완료하였습니다.", false),
    NOT_FOUND_POPULAR_REGION(HttpStatus.NOT_FOUND, "M201", "인기 지역 목록이 존재하지 않습니다.", false),
    FOUND_POPULAR_REGION(HttpStatus.OK, "M200", "인기 지역 조회 완료하였습니다.", false),

    // Approval
    SUCCESS_SEND_SMS(HttpStatus.OK, "A200", "인증번호 발송에 성공하었습니다.", false),
    FAIL_SEND_SMS(HttpStatus.INTERNAL_SERVER_ERROR, "A103", "인증번호 발송에 실패하였습니다.", false),
    ERROR_SEND_SMS(HttpStatus.INTERNAL_SERVER_ERROR, "A102", "인증문자 발송 중 오류가 발생하였습니다.", false),
    SUCCESS_CHECK_SMS(HttpStatus.OK, "A200", "인증이 완료되었습니다.", false),
    FAIL_CHECK_SMS(HttpStatus.BAD_REQUEST, "A300", "인증에 실패하였습니다.", false),

    // Schedule
    SUCCESS_SCHEDULE_CREATE(HttpStatus.CREATED, "S201", "일정 생성에 성공하였습니다.", false),
    SUCCESS_SCHEDULE_SAVE(HttpStatus.OK, "S202", "일정 저장에 성공하였습니다.", false),
    SUCCESS_SCHEDULE_DELETE(HttpStatus.OK, "S203", "일정 삭제에 성공하였습니다.", false),
    FOUND_INFO_SCHEDULE(HttpStatus.OK, "S202", "일정 조회에 성공하였습니다.", false),
    FAIL_SCHEDULE_SAVE(HttpStatus.INTERNAL_SERVER_ERROR, "S401", "일정 저장에 실패하였습니다.", false),
    NOT_FOUND_INFO_SCHEDULE(HttpStatus.NOT_FOUND, "S401", "일정 정보를 찾을 수 없습니다.", false),
    ALREADY_DELETED_SCHEDULE(HttpStatus.NOT_FOUND, "S402", "이미 삭제된 일정입니다.", false),

    // Ai
    AI_RECOMMENDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I101", "AI 응답에 실패하였습니다.", false),

    // Place
    PLACE_SEARCH_EMPTY(HttpStatus.NOT_FOUND, "P101", "지역 검색 결과를 찾을 수 없습니다.", false),

    // Tag
    SUCCESS_FOUND_TAG(HttpStatus.OK, "G200", "태그 조회에 성공하였습니다.", false),
    NOT_FOUND_TAG(HttpStatus.NOT_FOUND, "G101", "태그 정보를 찾을 수 없습니다.", false),
    INVALID_TAG_TYPE(HttpStatus.BAD_REQUEST, "G102", "유효하지 않은 태그 타입입니다.", false),

    // Item
    NOT_FOUND_ITEM(HttpStatus.NOT_FOUND, "I201", "아이템 정보를 찾을 수 없습니다.", false),

    // Category
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "T201", "카테고리 정보를 찾을 수 없습니다.", false),

    // Region
    NOT_FOUND_REGION(HttpStatus.NOT_FOUND, "R201", "지역 정보를 찾을 수 없습니다.", false);


    private final HttpStatus status;
    private final String code;
    private final String message;
    private final boolean notify;  // 알림 여부
}
