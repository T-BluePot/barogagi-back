package com.barogagi.member.repository.spec;

import com.barogagi.member.domain.RefreshToken;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class RefreshTokenSpec {

    public static Specification<RefreshToken> tokenEq(String refreshToken) {
        return (root, query, cb) ->
                refreshToken == null ? null : cb.equal(root.get("token"), refreshToken);
    }

    public static Specification<RefreshToken> statusValid() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), "VALID");
    }

    public static Specification<RefreshToken> notExpired() {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("expiresAt"), LocalDateTime.now());
    }
}

