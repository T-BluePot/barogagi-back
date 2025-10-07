package com.barogagi.member.login.repository;

import com.barogagi.member.login.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUserId(String userId);
}
