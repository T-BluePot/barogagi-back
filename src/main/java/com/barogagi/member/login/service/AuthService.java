package com.barogagi.member.login.service;

import com.barogagi.member.domain.MembershipStatus;
import com.barogagi.member.domain.UserMembershipInfo;
import com.barogagi.member.login.dto.*;
import com.barogagi.member.domain.RefreshToken;
import com.barogagi.member.login.exception.InvalidRefreshTokenException;
import com.barogagi.member.login.exception.LoginException;
import com.barogagi.member.repository.RefreshTokenRepository;
import com.barogagi.member.repository.UserMembershipRepository;
import com.barogagi.util.JwtUtil;
import com.barogagi.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserMembershipRepository userMembershipRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwt;

    @Value("${jwt.access-exp-seconds}")
    private long accessExp;
    @Value("${jwt.refresh-exp-seconds}")
    private long refreshExp;

    public LoginResponse login(LoginRequest req) {

        UserMembershipInfo userMembershipInfo = userMembershipRepository.findByUserId(req.userId());

        if (userMembershipInfo == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }

        String membershipNo = userMembershipInfo.getMembershipNo();
        String deviceId = req.deviceId();

        // 같은 회원 + 같은 기기의 기존 Refresh Token 삭제
        refreshTokenRepository.deleteByMembershipNoAndDeviceId(membershipNo, deviceId);
        refreshTokenRepository.flush();

        String access = jwt.generateAccessToken(membershipNo, userMembershipInfo.getUserId(), deviceId);

        String refresh = jwt.generateRefreshToken(membershipNo, deviceId);

        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(membershipNo);
        rt.setDeviceId(deviceId);
        rt.setToken(refresh);
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwt.getRefreshExpSeconds()));

        refreshTokenRepository.save(rt);

        return new LoginResponse(
                new TokenPair(
                        access,
                        jwt.getAccessExpSeconds(),
                        refresh,
                        jwt.getRefreshExpSeconds(),
                        ErrorCode.SUCCESS_LOGIN.getCode(),
                        ErrorCode.SUCCESS_LOGIN.getMessage()
                ),
                membershipNo,
                userMembershipInfo.getUserId(),
                userMembershipInfo.getJoinType(),
                deviceId
        );
    }

    /** 회원가입 직후: userId로 바로 토큰 발급 (비밀번호 검증 없음) */
    public LoginResponse loginAfterSignup(String userId, String deviceId) {
        UserMembershipInfo userMembershipInfo = userMembershipRepository.findByUserId(userId);

        if (userMembershipInfo == null) {
            throw new LoginException(ErrorCode.NOT_FOUND_USER_INFO);
        }

        if (MembershipStatus.WITHDRAWAL_PENDING == userMembershipInfo.getStatus()) {
            userMembershipRepository.restoreWithdrawal(
                    userMembershipInfo.getMembershipNo(), MembershipStatus.ACTIVE, MembershipStatus.WITHDRAWAL_PENDING
            );
        }

        String membershipNo = userMembershipInfo.getMembershipNo();

        // 같은 회원 + 같은 기기의 기존 Refresh Token만 삭제
        refreshTokenRepository.deleteByMembershipNoAndDeviceId(membershipNo, deviceId);
        refreshTokenRepository.flush();

        String access = jwt.generateAccessToken(membershipNo, userMembershipInfo.getUserId(), deviceId);
        String refresh = jwt.generateRefreshToken(membershipNo, deviceId);

        RefreshToken rt = new RefreshToken();
        rt.setMembershipNo(membershipNo);
        rt.setDeviceId(deviceId);
        rt.setToken(refresh);
        rt.setCreatedAt(LocalDateTime.now());
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(jwt.getRefreshExpSeconds()));

        refreshTokenRepository.save(rt);

        return new LoginResponse(
                new TokenPair(
                        access,
                        jwt.getAccessExpSeconds(),
                        refresh,
                        jwt.getRefreshExpSeconds(),
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getCode(),
                        ErrorCode.SUCCESS_REFRESH_TOKEN.getMessage()
                ),
                membershipNo,
                userMembershipInfo.getUserId(),
                userMembershipInfo.getJoinType(),
                deviceId
        );
    }

    @Transactional
    public TokenPair rotate(String refreshToken) {

        final Claims claims;

        try {
            // JWT 검증
            // - 서명
            // - issuer
            // - expiration
            // - typ = REFRESH
            claims = jwt.parseToken(refreshToken, "REFRESH");

        } catch (ExpiredJwtException e) {
            throw new InvalidRefreshTokenException(ErrorCode.REQUIRED_RE_LOGIN);

        } catch (JwtException | IllegalArgumentException | SecurityException e) {
            throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
        }

        String membershipNo = jwt.getMembershipNo(claims);
        String deviceId = jwt.getDeviceId(claims);

        // deviceId가 없는 비정상 토큰 방어
        if (membershipNo == null || membershipNo.isBlank() || deviceId == null || deviceId.isBlank()) {
            throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
        }

        // DB에 현재 Refresh Token이 존재하는지 확인
        RefreshToken current = refreshTokenRepository.findByToken(refreshToken)
                        .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.REQUIRED_LOGIN));

        // JWT와 DB의 회원번호 / deviceId 일치 여부 확인
        if (!membershipNo.equals(current.getMembershipNo()) || !deviceId.equals(current.getDeviceId())) {
            throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
        }

        // DB 만료 여부 확인
        if (current.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(current);
            throw new InvalidRefreshTokenException(ErrorCode.REQUIRED_RE_LOGIN);
        }

        // 회원 조회
        UserMembershipInfo user = userMembershipRepository.findById(membershipNo)
                        .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.NOT_FOUND_USER_INFO));

        // 기존 Refresh Token 삭제
        refreshTokenRepository.delete(current);

        // DB에 즉시 반영
        refreshTokenRepository.flush();

        // 새로운 Access Token
        String newAccess = jwt.generateAccessToken(membershipNo, user.getUserId(), deviceId);

        // 새로운 Refresh Token
        String newRefresh = jwt.generateRefreshToken(membershipNo, deviceId);

        // 새로운 Refresh Token 저장
        RefreshToken next = new RefreshToken();

        next.setMembershipNo(membershipNo);
        next.setDeviceId(deviceId);
        next.setToken(newRefresh);
        next.setCreatedAt(LocalDateTime.now());
        next.setExpiresAt(LocalDateTime.now().plusSeconds(jwt.getRefreshExpSeconds()));

        refreshTokenRepository.save(next);

        return new TokenPair(
                newAccess,
                jwt.getAccessExpSeconds(),
                newRefresh,
                jwt.getRefreshExpSeconds(),
                ErrorCode.SUCCESS_REFRESH_TOKEN.getCode(),
                ErrorCode.SUCCESS_REFRESH_TOKEN.getMessage()
        );
    }

    /** 현재 기기 로그아웃: refresh 기준 (membershipNo, deviceId)의 VALID 토큰들을 REVOKE */
    @Transactional
    public boolean logout(String refreshToken) {

        try {

            Claims claims = jwt.parseToken(refreshToken, "REFRESH");

            String membershipNo = jwt.getMembershipNo(claims);
            String deviceId = jwt.getDeviceId(claims);

            if (membershipNo == null || membershipNo.isBlank() || deviceId == null || deviceId.isBlank()) {
                return false;
            }

            RefreshToken current = refreshTokenRepository.findByToken(refreshToken).orElse(null);

            if (current == null) {
                return false;
            }

            if (!membershipNo.equals(current.getMembershipNo()) || !deviceId.equals(current.getDeviceId())) {
                return false;
            }
            refreshTokenRepository.delete(current);

            return true;

        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException | SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /** 현재 기기 로그아웃: 해당 회원 + 해당 기기의 Refresh Token 삭제 */
    @Transactional
    public void logoutAll(String membershipNo) {
        refreshTokenRepository.deleteAllByMembershipNo(membershipNo);
    }

    public Map<String, String> selectUserInfoByToken(String refreshToken) {
        Map<String, String> returnMap = new HashMap<>();
        try {
            Claims claims = jwt.parseToken(refreshToken, "REFRESH");
            String membershipNo = jwt.getMembershipNo(claims);
            String deviceId = jwt.getDeviceId(claims);

            if (membershipNo == null || membershipNo.isBlank() || deviceId == null || deviceId.isBlank()) {
                throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
            }

            RefreshToken current = refreshTokenRepository.findByToken(refreshToken)
                            .orElseThrow(() -> new InvalidRefreshTokenException(ErrorCode.NOT_FOUND_AVAILABLE_REFRESH_TOKEN));

            if (!membershipNo.equals(current.getMembershipNo()) || !deviceId.equals(current.getDeviceId())) {
                throw new InvalidRefreshTokenException(ErrorCode.UNAVAILABLE_REFRESH_TOKEN);
            }

            returnMap.put("membershipNo", membershipNo);
            returnMap.put("resultCode", "200");
            returnMap.put("message", "성공");

        } catch (ExpiredJwtException e) {
            returnMap.put("resultCode", ErrorCode.REQUIRED_RE_LOGIN.getCode());
            returnMap.put("message", ErrorCode.REQUIRED_RE_LOGIN.getMessage());

        } catch (InvalidRefreshTokenException e) {
            returnMap.put("resultCode", e.getCode());
            returnMap.put("message", e.getMessage());

        } catch (JwtException | IllegalArgumentException | SecurityException e) {
            returnMap.put("resultCode", ErrorCode.UNAVAILABLE_REFRESH_TOKEN.getCode());
            returnMap.put("message", ErrorCode.UNAVAILABLE_REFRESH_TOKEN.getMessage());
        }

        return returnMap;
    }
}

