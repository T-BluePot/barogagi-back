package com.barogagi.member.repository;

import com.barogagi.member.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, JpaSpecificationExecutor<RefreshToken> {

    Optional<RefreshToken> findByTokenAndStatus(String token, String status);

    List<RefreshToken> findByMembershipNoAndDeviceIdAndStatus(
            String membershipNo, String deviceId, String status
    );

    List<RefreshToken> findByMembershipNoAndStatus(String membershipNo, RefreshToken.Status status);

    int deleteAllByMembershipNo(String membershipNo);
}
