package com.barogagi.member.login.service;

import com.barogagi.member.login.dto.*;
import com.barogagi.member.login.entity.RefreshToken;
import com.barogagi.member.login.entity.UserMembership;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.mapper.AuthMapper;
import com.barogagi.member.login.repository.RefreshTokenRepository;
import com.barogagi.member.login.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import com.barogagi.util.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserMembershipRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final JwtUtil jwt;
    private final PasswordEncoder encoder;

    private final AuthMapper authMapper;

    @Value("${jwt.access-exp-seconds}")
    private long accessExp;
    @Value("${jwt.refresh-exp-seconds}")
    private long refreshExp;

    public AuthService(UserMembershipRepository userRepo, RefreshTokenRepository refreshRepo,
                       JwtUtil jwt, PasswordEncoder encoder,
                       AuthMapper authMapper) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.jwt = jwt;
        this.encoder = encoder;
        this.authMapper = authMapper;
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

        String no = u.getMembershipNo();
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
                new TokenPair(
                        access,
                        accessExp,
                        refresh,
                        refreshExp,
                        ErrorCode.SUCCESS_LOGIN.getCode(),
                        ErrorCode.SUCCESS_LOGIN.getMessage()
                ),
                no, u.getUserId(), u.getJoinType()
        );
    }

    /** 구글/네이버 등 OAuth 가입 직후: userId로 바로 토큰 발급 (비밀번호 검증 없음) */
    public LoginResponse loginAfterSignup(String userId, String deviceId) {
        UserMembership u = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String no = u.getMembershipNo();
        String access  = jwt.generateAccessToken(no, u.getUserId());
        String refresh = jwt.generateRefreshToken(no, deviceId != null ? deviceId : "web-oauth");

        // Refresh 저장(VALID)
        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(no);
        rt.setDeviceId(deviceId != null ? deviceId : "web-oauth");
        rt.setToken(refresh);
        rt.setStatus("VALID");
        rt.setCreatedAt(java.time.LocalDateTime.now());
        rt.setExpiresAt(java.time.LocalDateTime.now().plusSeconds(refreshExp));
        refreshRepo.save(rt);

        return new LoginResponse(
                new TokenPair(
                        access,
                        accessExp,
                        refresh,
                        refreshExp,
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getCode(),
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getMessage()
                ),
                no, u.getUserId(), u.getJoinType()
        );
    }

    @Transactional
    public TokenPair rotate(String refreshToken) {

        try {
            if (!jwt.isTokenValid(refreshToken) || !jwt.isRefreshToken(refreshToken)) {
                throw new BadCredentialsException("invalid_refresh_token");
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("invalid_refresh_token");
        }

        String newAccess = "";
        String newRefresh = "";

        String membershipNo = jwt.getMembershipNo(refreshToken);
        String deviceId = jwt.getDeviceId(refreshToken);

        logger.info("deviceId.isEmpty()={}", deviceId.isEmpty());
        if(deviceId.isEmpty()) {
            deviceId = "web-oauth";
        }

        try {

            // 현재 리프레시가 DB에 VALID로 존재하는지 확인
            RefreshToken current = refreshRepo.findByTokenAndStatus(refreshToken, "VALID")
                    .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.REQUIRED_LOGIN));

            // 만료 체크
            if (current.getExpiresAt().isBefore(LocalDateTime.now())) {
                current.setStatus("REVOKED");
                refreshRepo.save(current);
                throw new InvalidRefreshTokenException(ErrorCode.REQUIRED_RE_LOGIN);
            }

            // 같은 멤버/디바이스의 기존 VALID 토큰들 모두 REVOKE (동시 세션 차단용)
            List<RefreshToken> olds = refreshRepo.findByMembershipNoAndDeviceIdAndStatus(membershipNo, deviceId, "VALID");
            for (RefreshToken o : olds) {
                o.setStatus("REVOKED");
            }
            refreshRepo.saveAll(olds);

            // 새 토큰 발급
            UserMembership user = userRepo.findById(membershipNo)
                    .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.NOT_FOUND_USER_INFO));

            newAccess  = jwt.generateAccessToken(membershipNo, user.getUserId());
            newRefresh = jwt.generateRefreshToken(membershipNo, deviceId);

            RefreshToken next = new RefreshToken();
            next.setMembershipNo(membershipNo);
            next.setDeviceId(deviceId);
            next.setToken(newRefresh);
            next.setStatus("VALID");
            next.setCreatedAt(LocalDateTime.now());
            next.setExpiresAt(LocalDateTime.now().plusSeconds(jwt.getRefreshExpSeconds()));
            refreshRepo.save(next);

        } catch (InvalidRefreshTokenException ex) {
            return new TokenPair(
                    "",
                    0,
                    "",
                    0,
                    ex.getCode(),
                    ex.getMessage()
                    );
        }

        return new TokenPair(
                newAccess, jwt.getAccessExpSeconds(),
                newRefresh, jwt.getRefreshExpSeconds(),
                ErrorCode.SUCCESS_REFRESH_TOKEN.getCode(),
                ErrorCode.SUCCESS_REFRESH_TOKEN.getMessage()
        );
    }

    /** 현재 기기 로그아웃: refresh 기준 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE */
    @Transactional
    public void logout(String refreshToken) {
        if (!jwt.isTokenValid(refreshToken) || !jwt.isRefreshToken(refreshToken)) return;

        String membershipNo = jwt.getMembershipNo(refreshToken);
        String deviceId = jwt.getDeviceId(refreshToken);

        List<RefreshToken> tokens = refreshRepo
                .findByMembershipNoAndDeviceIdAndStatus(membershipNo, deviceId, "VALID");

        for (RefreshToken t : tokens) t.setStatus("REVOKED");
        if (!tokens.isEmpty()) refreshRepo.saveAll(tokens);
    }

    /** 모든 기기 로그아웃: 회원의 모든 VALID 리프레시 REVOKE */
    @Transactional
    public void logoutAll(String membershipNo) {
        List<RefreshToken> tokens = refreshRepo.findByMembershipNoAndStatus(membershipNo, RefreshToken.Status.VALID);

        for (var t : tokens) t.setStatus("REVOKED");
        if (!tokens.isEmpty()) refreshRepo.saveAll(tokens);
    }

    public Map<String, String> selectUserInfoByToken(String refreshToken) {

        Map<String, String> returnMap = new HashMap<>();

        String resultCode = "";
        String message = "";
        String membershipNo = "";

        try {
            // 1. JWT 토큰 유효성 검증
            if(!jwt.isTokenValid(refreshToken) || !jwt.isRefreshToken(refreshToken)) {
                throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
            }

            // 2. membershipNo 구하기
            membershipNo = authMapper.selectUserInfoByToken(refreshToken);

            // 3. membershipNo 조회가 되지 않을 경우
            if(membershipNo == null || membershipNo.isBlank()) {
                throw new InvalidRefreshTokenException(ErrorCode.NOT_FOUND_AVAILABLE_REFRESH_TOKEN);
            }

            resultCode = "200";
            message = "성공";
            returnMap.put("membershipNo", membershipNo);

        } catch (InvalidRefreshTokenException e) {
            resultCode = e.getCode();
            message = e.getMessage();
        } finally {
            returnMap.put("resultCode", resultCode);
            returnMap.put("message", message);
        }
        return returnMap;
    }
}

