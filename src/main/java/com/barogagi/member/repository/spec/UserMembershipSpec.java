package com.barogagi.member.repository.spec;

import com.barogagi.member.domain.UserMembershipInfo;
import org.springframework.data.jpa.domain.Specification;

public class UserMembershipSpec {

    public static Specification<UserMembershipInfo> userIdEq(String userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<UserMembershipInfo> emailEq(String email) {
        return (root, query, cb) ->
                email == null ? null : cb.equal(root.get("email"), email);
    }

    public static Specification<UserMembershipInfo> joinTypeEq(String joinType) {
        return (root, query, cb) ->
                joinType == null ? null : cb.equal(root.get("joinType"), joinType);
    }
}
