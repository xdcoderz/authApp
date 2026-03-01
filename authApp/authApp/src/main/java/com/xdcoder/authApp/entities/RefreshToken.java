package com.xdcoder.authApp.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refresh_tokens",indexes = {
            @Index(name="refresh_token_jti_idx", columnList = "jti",unique=true),
            @Index(name="refresh_token_user_idx", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="jti", nullable = false, unique = true, updatable = false)
    private String jti;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(updatable = false, nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private boolean revoked;
//    private String refreshToken;
    private String replacedByToken;

}
