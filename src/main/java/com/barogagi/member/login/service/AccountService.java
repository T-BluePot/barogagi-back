package com.barogagi.member.login.service;

import com.barogagi.member.login.repository.RefreshTokenRepository;
import com.barogagi.member.login.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMembershipRepository userMembershipRepository;

    @Transactional
    public void deleteMyAccount(Long membershipNo) {
        // 1) 모든 리프레시 토큰 제거
        refreshTokenRepository.deleteAllByMembershipNo(membershipNo);
        // 2) 회원 삭제
        userMembershipRepository.deleteByMembershipNo(membershipNo);
    }
}
