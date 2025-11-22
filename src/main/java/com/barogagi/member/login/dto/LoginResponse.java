package com.barogagi.member.login.dto;

public record LoginResponse(TokenPair tokens, String membershipNo, String userId, String joinType) {}
