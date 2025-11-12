package com.wallet.repository;

import com.wallet.model.UserSession;
import com.wallet.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    List<UserSession> findByUserId(UUID userId);

    List<UserSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    List<UserSession> findByStatus(SessionStatus status);

    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.status = 'ACTIVE'")
    int expireOldSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'LOGGED_OUT', s.loggedOutAt = :now WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    int logoutAllUserSessions(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    long countActiveSessionsByUserId(@Param("userId") UUID userId);

    boolean existsBySessionTokenAndStatus(String sessionToken, SessionStatus status);
}