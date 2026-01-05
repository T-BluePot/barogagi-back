package com.barogagi.member.login.repository;

import com.barogagi.member.login.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndStatus(String token, String status);

    List<RefreshToken> findByMembershipNoAndDeviceIdAndStatus(
            String membershipNo, String deviceId, String status
    );

    List<RefreshToken> findByMembershipNoAndStatus(String membershipNo, RefreshToken.Status status);

    int deleteAllByMembershipNo(String membershipNo);
}
