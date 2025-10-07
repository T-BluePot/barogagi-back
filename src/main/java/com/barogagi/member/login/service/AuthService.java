package com.barogagi.member.login.service;

import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.entity.RefreshToken;
import com.barogagi.member.login.entity.UserMembership;
import com.barogagi.member.login.repository.RefreshTokenRepository;
import com.barogagi.member.login.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final UserMembershipRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final JwtUtil jwt;
    private final PasswordEncoder encoder;

    @Value("${jwt.access-exp-seconds}")
    private long accessExp;
    @Value("${jwt.refresh-exp-seconds}")
    private long refreshExp;

    public AuthService(UserMembershipRepository userRepo, RefreshTokenRepository refreshRepo,
                       JwtUtil jwt, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.jwt = jwt;
        this.encoder = encoder;
    }

    public LoginResponse login(LoginRequest req) {
        UserMembership u = userRepo.findByUserId(req.userId())
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // BASIC 가입만 패스워드 검증 (소셜은 별도 플로우에서 토큰 교환 권장)
        if (!"BASIC".equalsIgnoreCase(u.getJoinType())) {
            throw new RuntimeException("NOT_BASIC_MEMBER");
        }
        if (u.getPassword() == null || !encoder.matches(req.password(), u.getPassword())) {
            throw new RuntimeException("BAD_CREDENTIALS");
        }

        Long no = u.getMembershipNo();
        String access = jwt.generateAccessToken(no, u.getUserId());
        String refresh = jwt.generateRefreshToken(no, req.deviceId());

        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(no);
        rt.setDeviceId(req.deviceId());
        rt.setToken(refresh);
        rt.setStatus("VALID");
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExp));
        refreshRepo.save(rt);

        return new LoginResponse(
                new TokenPair(access, accessExp, refresh, refreshExp),
                no, u.getUserId(), u.getJoinType()
        );
    }

    /** 구글/네이버 등 OAuth 가입 직후: userId로 바로 토큰 발급 (비밀번호 검증 없음) */
    public LoginResponse loginAfterOAuthSignup(String userId, String deviceId) {
        var u = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 선택: BASIC 사용자가 이 엔드포인트를 타지 못하게 막고 싶다면
        if ("BASIC".equalsIgnoreCase(u.getJoinType())) {
            throw new RuntimeException("NOT_OAUTH_MEMBER");
        }

        Long no = u.getMembershipNo();
        String access  = jwt.generateAccessToken(no, u.getUserId());
        String refresh = jwt.generateRefreshToken(no, deviceId != null ? deviceId : "web-oauth");

        // Refresh 저장(VALID)
        var rt = new RefreshToken();
        rt.setMembershipNo(no);
        rt.setDeviceId(deviceId != null ? deviceId : "web-oauth");
        rt.setToken(refresh);
        rt.setStatus("VALID");
        rt.setCreatedAt(java.time.LocalDateTime.now());
        rt.setExpiresAt(java.time.LocalDateTime.now().plusSeconds(refreshExp));
        refreshRepo.save(rt);

        return new LoginResponse(
                new TokenPair(access, accessExp, refresh, refreshExp),
                no, u.getUserId(), u.getJoinType()
        );
    }

    public TokenPair refresh(RefreshRequest req) {
        RefreshToken rt = refreshRepo.findByToken(req.refreshToken())
                .orElseThrow(() -> new RuntimeException("INVALID_REFRESH"));

        if (!"VALID".equals(rt.getStatus()) || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("EXPIRED_OR_REVOKED");
        }
        if (req.deviceId() != null && rt.getDeviceId() != null && !rt.getDeviceId().equals(req.deviceId())) {
            throw new RuntimeException("DEVICE_MISMATCH");
        }

        Long no = rt.getMembershipNo();
        UserMembership u = userRepo.findById(no).orElseThrow();

        String newAccess = jwt.generateAccessToken(no, u.getUserId());
        String newRefresh = jwt.generateRefreshToken(no, req.deviceId());

        // 회전: 이전 RT 폐기, 새 RT 저장
        rt.setStatus("REVOKED");
        refreshRepo.save(rt);

        RefreshToken next = new RefreshToken();
        next.setMembershipNo(no);
        next.setDeviceId(req.deviceId());
        next.setToken(newRefresh);
        next.setStatus("VALID");
        next.setCreatedAt(LocalDateTime.now());
        next.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExp));
        refreshRepo.save(next);

        return new TokenPair(newAccess, accessExp, newRefresh, refreshExp);
    }

    public void logout(LogoutRequest req) {
        refreshRepo.findByToken(req.refreshToken()).ifPresent(rt -> {
            if ("VALID".equals(rt.getStatus())) {
                rt.setStatus("REVOKED");
                refreshRepo.save(rt);
            }
        });
    }
}

