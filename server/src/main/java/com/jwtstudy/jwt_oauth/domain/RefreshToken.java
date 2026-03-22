package com.jwtstudy.jwt_oauth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false, name="expires_at")
    LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //같은 유저 RT 덮어쓰기
    public RefreshToken updateToken(String newRefreshToken, LocalDateTime newExpiresAt) {
        this.token = newRefreshToken;
        this.expiresAt = newExpiresAt;
        return this;
    }

    //RT 생성 - 정적 팩토리메서드
    public static RefreshToken build(User user, String token, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }
}
