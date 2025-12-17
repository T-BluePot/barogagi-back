package com.barogagi.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberResultCode {

    // nickname
    EMPTY_NICKNAME("101", "닉네임 정보를 입력해주세요."),
    INVALID_NICKNAME("102", "적합하지 않는 닉네임입니다."),
    UNAVAILABLE_NICKNAME("103", "해당 닉네임 사용이 불가능합니다."),
    AVAILABLE_NICKNAME("200", "사용 가능한 닉네임입니다."),

    // userId
    EMPTY_USER_ID("101", "아이디를 입력해주세요."),
    INVALID_USER_ID("102", "적합한 아이디가 아닙니다."),
    UNAVAILABLE_USER_ID("300", "해당 아이디 사용이 불가능합니다."),
    AVAILABLE_USER_ID("200", "해당 아이디 사용이 가능합니다."),

    // signUp
    EMPTY_SIGN_UP("101", "회원가입에 필요한 정보를 입력해주세요."),
    INVALID_SIGN_UP("102", "적합한 아이디, 비밀번호, 닉네임이 아닙니다."),
    SUCCESS_SIGN_UP("200", "회원가입에 성공하였습니다."),
    FAIL_SIGN_UP("300", "회원가입에 실패하였습니다."),

    // deleteAccount
    EMPTY_REFRESH_TOKEN("100", "refresh token이 존재하지 않습니다."),
    SUCCESS_DELETE_ACCOUNT("200", "회원 탈퇴되었습니다."),
    FAIL_DELETE_ACCOUNT("300", "회원 탈퇴 실패하였습니다.");

    private final String resultCode;
    private final String message;
}
