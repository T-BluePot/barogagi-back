package com.barogagi.member.login.service;

import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.domain.RefreshToken;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.repository.RefreshTokenRepository;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.member.service.RefreshTokenService;
import com.barogagi.util.JwtUtil;
import com.barogagi.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserMembershipRepository userMembershipRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwt;
    private final PasswordEncoder encoder;

    @Value("${jwt.access-exp-seconds}")
    private long accessExp;
    @Value("${jwt.refresh-exp-seconds}")
    private long refreshExp;

    public LoginResponse login(LoginRequest req) {
        UserMembershipInfo userMembershipInfo = userMembershipRepository.findByUserId(req.userId());

        if(null == userMembershipInfo) {
            throw new RuntimeException("USER_NOT_FOUND");
        }

        // BASIC 가입만 패스워드 검증 (소셜은 별도 플로우에서 토큰 교환 권장)
        if (!"BASIC".equalsIgnoreCase(userMembershipInfo.getJoinType())) {
            throw new RuntimeException("NOT_BASIC_MEMBER");
        }
        if (userMembershipInfo.getPassword() == null || !encoder.matches(req.password(), userMembershipInfo.getPassword())) {
            throw new RuntimeException("BAD_CREDENTIALS");
        }

        String no = userMembershipInfo.getMembershipNo();
        String access = jwt.generateAccessToken(no, userMembershipInfo.getUserId());
        String refresh = jwt.generateRefreshToken(no, req.deviceId());

        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(no);
        rt.setDeviceId(req.deviceId());
        rt.setToken(refresh);
        rt.setStatus("VALID");
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExp));
        refreshTokenRepository.save(rt);

        return new LoginResponse(
                new TokenPair(
                        access,
                        accessExp,
                        refresh,
                        refreshExp,
                        ErrorCode.SUCCESS_LOGIN.getCode(),
                        ErrorCode.SUCCESS_LOGIN.getMessage()
                ),
                no, userMembershipInfo.getUserId(), userMembershipInfo.getJoinType()
        );
    }

    /** 구글/네이버 등 OAuth 가입 직후: userId로 바로 토큰 발급 (비밀번호 검증 없음) */
    public LoginResponse loginAfterSignup(String userId, String deviceId) {
        UserMembershipInfo userMembershipInfo = userMembershipRepository.findByUserId(userId);

        if(null == userMembershipInfo) {
            throw new RuntimeException("USER_NOT_FOUND");
        }

        String membershipNo = userMembershipInfo.getMembershipNo();
        String access  = jwt.generateAccessToken(membershipNo, userMembershipInfo.getUserId());
        String refresh = jwt.generateRefreshToken(membershipNo, deviceId != null ? deviceId : "web-oauth");

        // 같은 멤버/디바이스의 기존 VALID 토큰들 모두 REVOKE (동시 세션 차단용)
        List<RefreshToken> olds = refreshTokenRepository.findByMembershipNoAndDeviceIdAndStatus(membershipNo, deviceId, "VALID");
        for (RefreshToken o : olds) {
            o.setStatus("REVOKED");
        }
        refreshTokenRepository.saveAll(olds);

        // Refresh 저장(VALID)
        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(membershipNo);
        rt.setDeviceId(deviceId != null ? deviceId : "web-oauth");
        rt.setToken(refresh);
        rt.setStatus("VALID");
        rt.setCreatedAt(java.time.LocalDateTime.now());
        rt.setExpiresAt(java.time.LocalDateTime.now().plusSeconds(refreshExp));
        refreshTokenRepository.save(rt);

        return new LoginResponse(
                new TokenPair(
                        access,
                        accessExp,
                        refresh,
                        refreshExp,
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getCode(),
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getMessage()
                ),
                membershipNo, userMembershipInfo.getUserId(), userMembershipInfo.getJoinType()
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

        if(deviceId.isEmpty()) {
            deviceId = "web-oauth";
        }

        try {

            // 현재 리프레시가 DB에 VALID로 존재하는지 확인
            RefreshToken current = refreshTokenRepository.findByTokenAndStatus(refreshToken, "VALID")
                    .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.REQUIRED_LOGIN));

            // 만료 체크
            if (current.getExpiresAt().isBefore(LocalDateTime.now())) {
                current.setStatus("REVOKED");
                refreshTokenRepository.save(current);
                throw new InvalidRefreshTokenException(ErrorCode.REQUIRED_RE_LOGIN);
            }

            // 같은 멤버/디바이스의 기존 VALID 토큰들 모두 REVOKE (동시 세션 차단용)
            List<RefreshToken> olds = refreshTokenRepository.findByMembershipNoAndDeviceIdAndStatus(membershipNo, deviceId, "VALID");
            for (RefreshToken o : olds) {
                o.setStatus("REVOKED");
            }
            refreshTokenRepository.saveAll(olds);

            // 새 토큰 발급
            UserMembershipInfo user = userMembershipRepository.findById(membershipNo)
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
            refreshTokenRepository.save(next);

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

        List<RefreshToken> tokens = refreshTokenRepository
                .findByMembershipNoAndDeviceIdAndStatus(membershipNo, deviceId, "VALID");

        for (RefreshToken t : tokens) t.setStatus("REVOKED");
        if (!tokens.isEmpty()) refreshTokenRepository.saveAll(tokens);
    }

    /** 모든 기기 로그아웃: 회원의 모든 VALID 리프레시 REVOKE */
    @Transactional
    public void logoutAll(String membershipNo) {
        List<RefreshToken> tokens = refreshTokenRepository.findByMembershipNoAndStatus(membershipNo, RefreshToken.Status.VALID);

        for (var t : tokens) t.setStatus("REVOKED");
        if (!tokens.isEmpty()) refreshTokenRepository.saveAll(tokens);
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
            membershipNo = refreshTokenService.selectUserInfoByToken(refreshToken);

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

