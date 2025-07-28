package com.voiz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyRaw; // application.properties에서 불러올 문자열

    private Key secretKey; // 실제 서명에 사용할 Key 객체

    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 30; // 30분
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일

    // 엑세스 토큰 생성
    public String createAccessToken(String username, String role, String accessUuid, String userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.setId(accessUuid); // jti 설정
        claims.put("userId", userId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String username) {
        return createToken(username, null, REFRESH_TOKEN_VALIDITY);
    }

    private String createToken(String username, String role, long validity) {
        Claims claims = Jwts.claims().setSubject(username);
        if (role != null) {
            claims.put("role", role);
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKeyRaw.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("🔐 [JwtTokenProvider] Loaded key with length: " + keyBytes.length);
    }
}
