package com.voiz.service;

import com.voiz.mapper.TokenRepository;
import com.voiz.vo.Token;
import com.voiz.vo.Users;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMilliseconds;

    @Autowired
    private TokenRepository tokenRepository;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, String> generateTokens(Users user) {
        String accessUuid = UUID.randomUUID().toString();
        
        Date accessTokenExpiration = new Date(System.currentTimeMillis() + accessTokenValidityInMilliseconds);
        Date refreshTokenExpiration = new Date(System.currentTimeMillis() + refreshTokenValidityInMilliseconds);

        String accessToken = Jwts.builder()
                .setSubject(user.getUserId())
                .claim("uuid", accessUuid)
                .claim("storeName", user.getStoreName())
                .claim("storeCategory", user.getStoreCategory())
                .claim("userName", user.getUserName())
                .setIssuedAt(new Date())
                .setExpiration(accessTokenExpiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(user.getUserId())
                .claim("uuid", accessUuid)
                .setIssuedAt(new Date())
                .setExpiration(refreshTokenExpiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        tokenRepository.deleteByUserId(user.getUserId());

        Token tokenEntity = new Token();
        tokenEntity.setUserId(user.getUserId());
        tokenEntity.setAccessUuid(accessUuid);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setExpiresAt(refreshTokenExpiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());

        tokenRepository.save(tokenEntity);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");
        
        return tokens;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getAccessUuidFromToken(String token) {
        return getClaimsFromToken(token).get("uuid", String.class);
    }

    public Map<String, String> refreshAccessToken(String refreshToken) {
        try {
            if (!validateToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            Claims claims = getClaimsFromToken(refreshToken);
            String userId = claims.getSubject();
            String accessUuid = claims.get("uuid", String.class);

            Token tokenEntity = tokenRepository.findByRefreshToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found in database"));

            if (!tokenEntity.getUserId().equals(userId) || 
                !tokenEntity.getAccessUuid().equals(accessUuid)) {
                throw new RuntimeException("Token mismatch");
            }

            if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                tokenRepository.delete(tokenEntity);
                throw new RuntimeException("Refresh token expired");
            }

            String newAccessUuid = UUID.randomUUID().toString();
            Date accessTokenExpiration = new Date(System.currentTimeMillis() + accessTokenValidityInMilliseconds);

            String newAccessToken = Jwts.builder()
                    .setSubject(userId)
                    .claim("uuid", newAccessUuid)
                    .claim("storeName", claims.get("storeName"))
                    .claim("storeCategory", claims.get("storeCategory"))
                    .claim("userName", claims.get("userName"))
                    .setIssuedAt(new Date())
                    .setExpiration(accessTokenExpiration)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            tokenEntity.setAccessUuid(newAccessUuid);
            tokenRepository.save(tokenEntity);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", refreshToken);
            tokens.put("tokenType", "Bearer");
            
            return tokens;

        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage());
        }
    }

    public void revokeToken(String userId) {
        tokenRepository.deleteByUserId(userId);
    }

    public void updateLastUsedTime(String accessUuid) {
        tokenRepository.updateLastUsedAt(accessUuid, LocalDateTime.now());
    }

    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}