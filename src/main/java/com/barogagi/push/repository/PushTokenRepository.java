package com.barogagi.push.repository;

import com.barogagi.push.entity.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByMembershipNo(String membershipNo);
    List<PushToken> findByMembershipNoAndActiveYn(String membershipNo, String activeYn);
    List<PushToken> findAllByMembershipNoAndActiveYn(String membershipNo, String activeYn);
}
