package com.voiz.vo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_TOKENS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    @Id
    @Column(name = "TOKEN_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_seq")
    @SequenceGenerator(name = "token_seq", sequenceName = "VOYZ_TOKENS_SEQ", allocationSize = 1)
    private Long tokenId;
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
    
    @Column(name = "ACCESS_UUID", nullable = false, unique = true)
    private String accessUuid;
    
    @Column(name = "REFRESH_TOKEN", nullable = false, length = 500)
    private String refreshToken;
    
    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "LAST_USED_AT")
    private LocalDateTime lastUsedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUsedAt = LocalDateTime.now();
    }
}