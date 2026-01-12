package com.barogagi.member.service.impl;

import com.barogagi.member.domain.RefreshToken;
import com.barogagi.member.repository.RefreshTokenRepository;
import com.barogagi.member.repository.spec.RefreshTokenSpec;
import com.barogagi.member.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public String selectUserInfoByToken(String refreshToken) {

        Specification<RefreshToken> spec = RefreshTokenSpec.tokenEq(refreshToken)
                .and(RefreshTokenSpec.statusValid())
                .and(RefreshTokenSpec.notExpired());

        return refreshTokenRepository.findOne(spec)
                .map(RefreshToken::getMembershipNo)
                .orElse(null);
    }
}
