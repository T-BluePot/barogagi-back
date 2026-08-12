package com.barogagi.member.repository;

import com.barogagi.member.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, JpaSpecificationExecutor<RefreshToken> {

    /**
     * 특정 회원의 특정 기기에 저장된 Refresh Token 삭제
     */
    void deleteByMembershipNoAndDeviceId(
            String membershipNo,
            String deviceId
    );

    /**
     * Refresh Token 자체로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 특정 회원의 모든 Refresh Token 삭제
     */
    void deleteAllByMembershipNo(String membershipNo);
}