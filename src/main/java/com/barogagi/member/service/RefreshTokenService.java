package com.barogagi.member.service;

public interface RefreshTokenService {

    String selectUserInfoByToken(String refreshToken);
}
