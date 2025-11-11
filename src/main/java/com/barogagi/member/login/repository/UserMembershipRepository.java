package com.barogagi.member.login.repository;

import com.barogagi.member.login.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUserId(String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from USER_MEMBERSHIP_INFO u where u.membershipNo = :membershipNo")
    int deleteByMembershipNo(@Param("membershipNo") Long membershipNo);
}
