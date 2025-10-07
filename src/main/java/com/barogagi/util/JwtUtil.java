package com.barogagi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final JwtParser parser;
    private final String issuer;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtUtil(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.issuer:barogagi}") String issuer,
            @Value("${jwt.access-exp-seconds}") long accessExpSeconds,
            @Value("${jwt.refresh-exp-seconds}") long refreshExpSeconds
    ) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret(Base64)가 비어있습니다.");
        }
        byte[] secretBytes = Decoders.BASE64.decode(base64Secret);
        if (secretBytes.length < 32) { // 256bit
            throw new IllegalArgumentException("jwt.secret는 Base64로 인코딩된 32바이트(256비트) 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = (issuer == null || issuer.isBlank()) ? "barogagi" : issuer;
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;

        this.parser = Jwts.parserBuilder()
                .requireIssuer(this.issuer)
                .setSigningKey(this.key)
                .build();
    }

    /** Access 토큰 발급: sub=membershipNo, uid=userId, typ=ACCESS */
    public String generateAccessToken(Long membershipNo, String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(membershipNo))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .claim("uid", userId)      // 로그인 식별용(표시/트레이싱)
                .claim("typ", "ACCESS")    // 토큰 종류
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh 토큰 발급: sub=membershipNo, did=deviceId, typ=REFRESH */
    public String generateRefreshToken(Long membershipNo, String deviceId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(membershipNo))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .claim("did", deviceId == null ? "default" : deviceId)
                .claim("typ", "REFRESH")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 서명/만료 검증(ACCESS/REFRESH 공통). 유효하면 Claims 반환, 아니면 예외 */
    public Claims parseClaims(String token) throws JwtException, IllegalArgumentException {
        return parser.parseClaimsJws(token).getBody();
    }

    /** 만료/서명 포함 전반 유효성 확인(예외를 boolean으로 감싸서 리턴) */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** typ == ACCESS 인지 */
    public boolean isAccessToken(String token) {
        try {
            return "ACCESS".equalsIgnoreCase(parseClaims(token).get("typ", String.class));
        } catch (Exception e) { return false; }
    }

    /** typ == REFRESH 인지 */
    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equalsIgnoreCase(parseClaims(token).get("typ", String.class));
        } catch (Exception e) { return false; }
    }

    public String getUserId(String accessToken) {
        return parseClaims(accessToken).get("uid", String.class);
    }

    public String getDeviceId(String refreshToken) {
        return parseClaims(refreshToken).get("did", String.class);
    }

    public long getAccessExpSeconds() { return accessExpSeconds; }
    public long getRefreshExpSeconds() { return refreshExpSeconds; }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean isExpired(String token) {
        try {
            return parse(token).getBody().getExpiration().before(new Date());
        } catch (Exception e) { return true; }
    }

    public Long getMembershipNo(String token) {
        String sub = parse(token).getBody().getSubject();
        return Long.parseLong(sub);
    }
}