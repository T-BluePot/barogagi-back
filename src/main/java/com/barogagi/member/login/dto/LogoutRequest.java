package com.barogagi.member.login.dto;

public record LogoutRequest(String refreshToken, String deviceId) {}
