package com.wallet.model;

import com.wallet.model.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_token", columnList = "session_token", unique = true),
        @Index(name = "idx_session_user_id", columnList = "user_id"),
        @Index(name = "idx_session_status", columnList = "status"),
        @Index(name = "idx_session_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "logged_out_at")
    private LocalDateTime loggedOutAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE && !isExpired();
    }

    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void logout() {
        this.status = SessionStatus.LOGGED_OUT;
        this.loggedOutAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = SessionStatus.EXPIRED;
    }

}