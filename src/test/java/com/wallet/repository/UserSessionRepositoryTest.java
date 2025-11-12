package com.wallet.repository;

import com.wallet.model.User;
import com.wallet.model.UserSession;
import com.wallet.model.enums.SessionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserSessionRepositoryTest {

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserSession testSession;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();
        testUser = userRepository.save(testUser);

        testSession = UserSession.builder()
                .user(testUser)
                .sessionToken("token123")
                .ipAddress("192.168.1.1")
//                .userAgent("Mozilla/5.0")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(SessionStatus.ACTIVE)
                .build();
        sessionRepository.save(testSession);
    }

    @Test
    void testSaveSession() {
        UserSession saved = sessionRepository.save(testSession);

        assertNotNull(saved.getId());
        assertEquals("token123", saved.getSessionToken());
    }

    @Test
    void testFindBySessionToken() {
        Optional<UserSession> found = sessionRepository.findBySessionToken("token123");

        assertTrue(found.isPresent());
        assertEquals(testSession.getId(), found.get().getId());
    }

    @Test
    void testFindByUserId() {
        List<UserSession> sessions = sessionRepository.findByUserId(testUser.getId());

        assertEquals(1, sessions.size());
        assertEquals(testSession.getId(), sessions.get(0).getId());
    }

    @Test
    void testFindByUserIdAndStatus() {
        List<UserSession> activeSessions = sessionRepository.findByUserIdAndStatus(
                testUser.getId(),
                SessionStatus.ACTIVE
        );

        assertEquals(1, activeSessions.size());
        assertEquals(SessionStatus.ACTIVE, activeSessions.get(0).getStatus());
    }

    @Test
    void testFindActiveSessionsByUserId() {
        List<UserSession> active = sessionRepository.findActiveSessionsByUserId(testUser.getId());

        assertEquals(1, active.size());
    }

    @Test
    void testCountActiveSessionsByUserId() {
        long count = sessionRepository.countActiveSessionsByUserId(testUser.getId());

        assertEquals(1, count);
    }

    @Test
    void testExistsBySessionTokenAndStatus() {
        assertTrue(sessionRepository.existsBySessionTokenAndStatus("token123", SessionStatus.ACTIVE));
        assertFalse(sessionRepository.existsBySessionTokenAndStatus("token123", SessionStatus.LOGGED_OUT));
    }
}