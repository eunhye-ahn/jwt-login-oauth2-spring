package com.jwtstudy.jwt_oauth.repository;

import com.jwtstudy.jwt_oauth.domain.RefreshToken;
import com.jwtstudy.jwt_oauth.domain.User;
import com.jwtstudy.jwt_oauth.service.RefreshTokenService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String refreshToken);

    boolean existsByUser(User user);

    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}
