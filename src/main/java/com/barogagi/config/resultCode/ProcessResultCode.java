package com.barogagi.config.resultCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcessResultCode {

    EMPTY_DATA("101", "정보를 입력해주세요."),

    // nickname
    INVALID_NICKNAME("102", "적합하지 않는 닉네임입니다."),
    UNAVAILABLE_NICKNAME("103", "해당 닉네임 사용이 불가능합니다."),
    AVAILABLE_NICKNAME("200", "사용 가능한 닉네임입니다."),

    // userId
    INVALID_USER_ID("102", "적합한 아이디가 아닙니다."),
    UNAVAILABLE_USER_ID("300", "해당 아이디 사용이 불가능합니다."),
    AVAILABLE_USER_ID("200", "해당 아이디 사용이 가능합니다."),

    // signUp
    INVALID_SIGN_UP("102", "적합한 아이디, 비밀번호, 닉네임이 아닙니다."),
    SUCCESS_SIGN_UP("200", "회원가입에 성공하였습니다."),
    FAIL_SIGN_UP("300", "회원가입에 실패하였습니다."),

    // deleteAccount
    SUCCESS_DELETE_ACCOUNT("200", "회원 탈퇴되었습니다."),
    FAIL_DELETE_ACCOUNT("300", "회원 탈퇴 실패하였습니다."),

    // findUser
    FOUND_ACCOUNT("200", "해당 전화번호로 가입된 아이디가 존재합니다."),
    NOT_FOUND_ACCOUNT("201", "해당 전화번호로 가입된 계정이 존재하지 않습니다."),

    // updatePassword
    SUCCESS_UPDATE_PASSWORD("200", "비밀번호 재설정에 성공하였습니다."),
    FAIL_UPDATE_PASSWORD("300", "비밀번호 재설정에 실패하였습니다."),

    // Login
    NOT_FOUND_USER_INFO("102", "회원 정보가 존재하지 않습니다."),
    FAIL_LOGIN("103", "로그인에 실패하였습니다."),
    SUCCESS_LOGIN("200", "로그인에 성공하였습니다."),

    // refreshToken
    REQUIRED_LOGIN("110", "로그인을 진행해주세요."),
    REQUIRED_RE_LOGIN("120", "로그인을 다시 진행해주세요."),
    SUCCESS_REFRESH_TOKEN("200", "토큰이 재발급되었습니다."),

    // logout
    FAIL_LOGOUT("300", "로그아웃 실패하였습니다."),
    SUCCESS_LOGOUT("200", "로그아웃 되었습니다."),

    // terms
    FOUND_TERMS("200", "약관 조회에 성공하였습니다."),
    NOT_FOUND_TERMS("102", "약관이 존재하지 않습니다."),
    SUCCESS_INSERT_TERMS("200", "약관 저장에 성공하였습니다."),
    FAIL_INSERT_TERMS("300", "약관 저장에 실패하였습니다."),

    // memberInfo
    FOUND_USER_INFO("200", "회원 정보 조회가 완료되었습니다."),
    FAIL_UPDATE_USER_INFO("404", "사용자 정보 수정 실패하였습니다."),
    SUCCESS_UPDATE_USER_INFO("200", "사용자 정보 수정 완료하였습니다."),

    // mainPage
    NOT_FOUND_SCHEDULE("201", "일정이 존재하지 않습니다."),
    FOUND_SCHEDULE("v", "조회 성공하였습니다."),
    NOT_FOUND_POPULAR_TAG("201", "인기 태그 목록이 존재하지 않습니다."),
    FOUND_POPULAR_TAG("200", "인기 태그 조회 완료하였습니다."),
    NOT_FOUND_POPULAR_REGION("201", "인기 지역 목록이 존재하지 않습니다."),
    FOUND_POPULAR_REGION("200", "인기 지역 조회 완료하였습니다."),

    // approval
    SUCCESS_SEND_SMS("200", "인증번호 발송에 성공하었습니다."),
    FAIL_SEND_SMS("103", "인증번호 발송에 실패하였습니다."),
    ERROR_SEND_SMS("102", "오류가 발생하였습니다."),
    SUCCESS_CHECK_SMS("200", "인증이 완료되었습니다."),
    FAIL_CHECK_SMS("300", "인증에 실패하였습니다.");

    private final String resultCode;
    private final String message;
}
