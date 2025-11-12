package com.wallet.model;

import com.wallet.model.enums.BlockchainNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();

        wallet = Wallet.builder()
                .user(user)
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .address("0x1234567890abcdef")
                .encryptedPrivateKey("encrypted")
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    @Test
    void testWalletCreation() {
        assertNotNull(wallet);
        assertEquals(BlockchainNetwork.ETHEREUM_SEPOLIA, wallet.getNetwork());
        assertEquals("0x1234567890abcdef", wallet.getAddress());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertTrue(wallet.getActive());
    }

    @Test
    void testWalletBuilder() {
        Wallet builtWallet = Wallet.builder()
                .address("0xabc")
                .network(BlockchainNetwork.TRON)
                .balance(new BigDecimal("100"))
                .build();

        assertEquals("0xabc", builtWallet.getAddress());
        assertEquals(new BigDecimal("100"), builtWallet.getBalance());
    }

    @Test
    void testAddTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionHash("0xhash");

        wallet.addTransaction(transaction);

        assertEquals(1, wallet.getTransactions().size());
        assertEquals(wallet, transaction.getWallet());
    }
}