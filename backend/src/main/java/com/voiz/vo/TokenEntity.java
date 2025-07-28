package com.voiz.vo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VOYZ_TOKENS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEntity {

    @Id
    // ID 생성 -> SEQUENCE로 
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokens_seq_generator")
    @SequenceGenerator(
        name = "tokens_seq_generator",
        sequenceName = "TOKENS_SEQ", // 1단계에서 생성한 시퀀스 이름
        allocationSize = 1 // 증가값은 1
    )
    @Column(name = "TOKEN_ID")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_TOKENS_USERS"))
    private Users users;

    @Column(name = "ACCESS_UUID", length = 64)
    private String accessUuid;

    @Column(name = "REFRESH_TOKEN", length = 512)
    private String refreshToken;

    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "LAST_USED_AT")
    private LocalDateTime lastUsedAt;

    
}