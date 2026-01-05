package com.barogagi.member.login.repository;

import com.barogagi.member.login.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, String> {
    Optional<UserMembership> findByUserId(String userId);

    int deleteByMembershipNo(String membershipNo);
}
