package com.ridoh.aibankingassistant.ai_banking_assistant.auth.repository;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    Page<RefreshToken> findByUserAndRevokedFalse(
            User user,
            Pageable pageable
    );

    List<RefreshToken> findByUserAndRevokedFalse(User user);

    Optional<RefreshToken> findBySessionIdAndRevokedFalse(UUID sessionId);

    @Modifying
    @Query("""
        UPDATE RefreshToken rt
           SET rt.revoked = true
         WHERE rt.sessionId = :sessionId
           AND rt.revoked = false
    """)
    int revokeSession(
            @Param("sessionId") UUID sessionId
    );

    @Modifying
    @Query("""
        UPDATE RefreshToken rt
           SET rt.revoked = true
         WHERE rt.user = :user
           AND rt.sessionId <> :currentSessionId
           AND rt.revoked = false
    """)
    int revokeOtherSessions(
            @Param("user") User user,
            @Param("currentSessionId") UUID currentSessionId
    );

    @Modifying
    @Query("""
        UPDATE RefreshToken rt
           SET rt.revoked = true
         WHERE rt.user = :user
           AND rt.revoked = false
    """)
    int revokeAllSessions(
            @Param("user") User user
    );

    void deleteByUser(User user);
}