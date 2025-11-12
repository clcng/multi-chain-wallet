package com.wallet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .locked(false)
                .build();
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getEnabled());
        assertFalse(user.getLocked());
    }

    @Test
    void testUserBuilder() {
        User builtUser = User.builder()
                .username("builder")
                .email("builder@example.com")
                .password("pass")
                .build();

        assertEquals("builder", builtUser.getUsername());
        assertTrue(builtUser.getEnabled());
        assertFalse(builtUser.getLocked());
    }

    @Test
    void testAddWallet() {
        Wallet wallet = new Wallet();
        wallet.setAddress("0x123");

        user.addWallet(wallet);

        assertEquals(1, user.getWallets().size());
        assertEquals(user, wallet.getUser());
    }

    @Test
    void testRemoveWallet() {
        Wallet wallet = new Wallet();
        wallet.setAddress("0x123");

        user.addWallet(wallet);
        user.removeWallet(wallet);

        assertEquals(0, user.getWallets().size());
        assertNull(wallet.getUser());
    }
}