package com.barogagi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key;          // 단일 키
    private final JwtParser parser;
    private final String issuer;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;
    private final long clockSkewSeconds = 60;  // 만료시간 오차 허용(선택)

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

    public String generateAccessToken(String membershipNo, String userId, String deviceId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(membershipNo)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .claim("uid", userId)
                .claim("did", deviceId)
                .claim("typ", "ACCESS")
                .setId(UUID.randomUUID().toString())
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
                .claim("did", deviceId)
                .claim("typ", "REFRESH")
                .setId(UUID.randomUUID().toString())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
        토큰이 유효한지 체크
     */
    public Claims parseAndValidate(String jwt) {
        return parser.parseClaimsJws(jwt).getBody();
    }

    /*
        token이 유효한지 체크
        - typ : ACCESS / REFRESH
     */
    public Claims parseToken(String jwt, String typ) {
        Claims claims = parseAndValidate(jwt);

        if(!typ.equals(claims.get("typ", String.class))) {
            throw new SecurityException("Not our token");
        }

        return claims;
    }

    public String getMembershipNo(Claims claims) {
        return claims.getSubject();
    }

    public String getUserId(Claims claims) {
        return claims.get("uid", String.class);
    }

    public String getDeviceId(Claims claims) {
        return claims.get("did", String.class);
    }

    public long getAccessExpSeconds() { return accessExpSeconds; }
    public long getRefreshExpSeconds() { return refreshExpSeconds; }
}
