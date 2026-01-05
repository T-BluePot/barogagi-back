package com.barogagi.member.basic.join.repository;

import com.barogagi.member.basic.join.entity.UserMembershipInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicJoinRepository extends JpaRepository<UserMembershipInfo, String> {
    boolean existsByUserId(String userId);
    boolean existsByNickName(String nickName);
}
