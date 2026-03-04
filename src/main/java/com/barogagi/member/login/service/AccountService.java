package com.barogagi.member.login.service;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.repository.RefreshTokenRepository;
import com.barogagi.member.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMembershipRepository userMembershipRepository;

    @Transactional
    public int deleteMyAccount(String membershipNo, int reasonNo, String withdrawReason) {

        int result = 0;

        // 1) 모든 리프레시 토큰 제거
        int deleteRefreshToken = refreshTokenRepository.deleteAllByMembershipNo(membershipNo);

        // 2) 회원 정보에서 STATUS = WITHDRAWAL_PENDING, DEL_DATE = 탈퇴일 저장
        int updateDelInfo = userMembershipRepository.updateWithdrawalPending(
                membershipNo,
                MembershipStatus.WITHDRAWAL_PENDING,
                LocalDateTime.now().plusDays(7),
                reasonNo,
                withdrawReason
        );

        if(deleteRefreshToken > 0 && updateDelInfo > 0) {
            result = 1;
        }

        return result;
    }
}
