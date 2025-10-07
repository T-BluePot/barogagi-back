package com.barogagi.member.login.dto;

public record LoginResponse(TokenPair tokens, Long membershipNo, String userId, String joinType) {}
