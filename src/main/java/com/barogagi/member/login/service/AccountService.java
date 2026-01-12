package com.barogagi.member.login.service;

import com.barogagi.member.repository.RefreshTokenRepository;
import com.barogagi.member.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMembershipRepository userMembershipRepository;

    @Transactional
    public int deleteMyAccount(String membershipNo) {

        int result = 0;

        // 1) 모든 리프레시 토큰 제거
        int deleteRefreshToken = refreshTokenRepository.deleteAllByMembershipNo(membershipNo);

        // 2) 회원 삭제
        int deleteByMembershipNo = userMembershipRepository.deleteByMembershipNo(membershipNo);

        if(deleteRefreshToken > 0 && deleteByMembershipNo > 0) {
            result = 1;
        }

        return result;
    }
}
