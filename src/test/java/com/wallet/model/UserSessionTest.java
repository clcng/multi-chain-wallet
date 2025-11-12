package com.wallet.model;

import com.wallet.model.enums.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    private UserSession session;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();

        session = UserSession.builder()
                .user(user)
                .sessionToken("token123")
                .ipAddress("192.168.1.1")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(SessionStatus.ACTIVE)
                .build();
    }

    @Test
    void testSessionCreation() {
        assertNotNull(session);
        assertEquals("token123", session.getSessionToken());
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertFalse(session.isExpired());
    }

    @Test
    void testIsExpired() {
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        assertTrue(session.isExpired());
    }

    @Test
    void testIsActive() {
        assertTrue(session.isActive());

        session.setStatus(SessionStatus.LOGGED_OUT);
        assertFalse(session.isActive());
    }

    @Test
    void testLogout() {
        session.logout();

        assertEquals(SessionStatus.LOGGED_OUT, session.getStatus());
        assertNotNull(session.getLoggedOutAt());
    }

    @Test
    void testUpdateActivity() {
        LocalDateTime before = LocalDateTime.now();
        session.updateActivity();

        assertNotNull(session.getLastActivityAt());
        assertTrue(session.getLastActivityAt().isAfter(before) ||
                session.getLastActivityAt().isEqual(before));
    }
}