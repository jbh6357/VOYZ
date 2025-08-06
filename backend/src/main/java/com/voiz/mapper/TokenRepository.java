package com.voiz.mapper;

import com.voiz.vo.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findByAccessUuid(String accessUuid);
    
    Optional<Token> findByRefreshToken(String refreshToken);
    
    Optional<Token> findByUserId(String userId);
    
    @Modifying
    @Query("DELETE FROM Token t WHERE t.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Query("UPDATE Token t SET t.lastUsedAt = :lastUsedAt WHERE t.accessUuid = :accessUuid")
    void updateLastUsedAt(@Param("accessUuid") String accessUuid, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}