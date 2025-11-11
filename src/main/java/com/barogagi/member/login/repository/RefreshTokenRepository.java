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
            Long membershipNo, String deviceId, String status
    );

    List<RefreshToken> findByMembershipNoAndDeviceIdAndStatus(Long membershipNo, String deviceId, RefreshToken.Status status);

    List<RefreshToken> findByMembershipNoAndStatus(Long membershipNo, RefreshToken.Status status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from REFRESH_TOKEN r where r.membershipNo = :membershipNo")
    int deleteAllByMembershipNo(@Param("membershipNo") Long membershipNo);
}
