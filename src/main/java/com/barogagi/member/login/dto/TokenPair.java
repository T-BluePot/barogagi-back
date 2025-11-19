package com.barogagi.member.login.dto;

public record TokenPair(
        String accessToken, long accessTokenExpiresIn,
        String refreshToken, long refreshTokenExpiresIn
) {}
