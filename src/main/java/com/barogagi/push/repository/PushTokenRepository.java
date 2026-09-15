package com.barogagi.push.repository;

import com.barogagi.push.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByMembershipNoAndDeviceTypeAndActiveYn(String membershipNo,
                                                                   String deviceType,
                                                                   String activeYn);

    List<PushToken> findByMembershipNoAndActiveYn(String membershipNo, String activeYn);

    @Modifying
    @Query("""
            DELETE FROM PushToken p
            WHERE p.membershipNo = :membershipNo
            AND p.activeYn = :activeYn
            """)
    void deleteByMembershipNoAndActiveYn(@Param("membershipNo") String membershipNo,
                                                     @Param("activeYn") String activeYn);
    @Modifying
    @Query("""
            DELETE FROM PushToken p
            WHERE p.membershipNo = :membershipNo
            AND p.fcmToken = :fcmToken
            AND p.deviceType = :deviceType
            AND p.activeYn = :activeYn
            """)
    void deleteByMembershipNoAndFcmTokenAndDeviceTypeAndActiveYn(@Param("membershipNo") String membershipNo,
                                                                 @Param("fcmToken") String fcmToken,
                                                                 @Param("deviceType") String deviceType,
                                                                 @Param("activeYn") String activeYn);
}
