package com.barogagi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;          // 단일 키
    private final JwtParser parser;
    private final String issuer;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final long clockSkewSeconds = 30;  // 만료시간 오차 허용(선택)

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
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("jwt.secret는 Base64로 인코딩된 32바이트(256비트) 이상이어야 합니다.");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = (issuer == null || issuer.isBlank()) ? "barogagi" : issuer;
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;

        this.parser = Jwts.parserBuilder()
                .requireIssuer(this.issuer)
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .setSigningKey(this.key)
                .build();
    }

    public String generateAccessToken(String membershipNo, String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(membershipNo)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .claim("uid", userId)
                .claim("typ", "ACCESS")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String membershipNo, String deviceId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(membershipNo)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .claim("did", deviceId == null ? "default" : deviceId)
                .claim("typ", "REFRESH")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return parser.parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public boolean isAccessToken(String token) {
        try { return "ACCESS".equalsIgnoreCase(parseClaims(token).get("typ", String.class)); }
        catch (Exception e) { return false; }
    }

    public boolean isRefreshToken(String token) {
        try { return "REFRESH".equalsIgnoreCase(parseClaims(token).get("typ", String.class)); }
        catch (Exception e) { return false; }
    }

    public String getUserId(String accessToken) {
        return parseClaims(accessToken).get("uid", String.class);
    }

    public String getDeviceId(String refreshToken) {
        return parseClaims(refreshToken).get("did", String.class);
    }

    public long getAccessExpSeconds() { return accessExpSeconds; }
    public long getRefreshExpSeconds() { return refreshExpSeconds; }

    public boolean isExpired(String token) {
        try { return parseClaims(token).getExpiration().before(new Date()); }
        catch (Exception e) { return true; }
    }

    public String getMembershipNo(String token) {
        return String.valueOf(parseClaims(token).getSubject());
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            String typ = claims.get("typ", String.class);
            return "ACCESS".equals(typ);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
