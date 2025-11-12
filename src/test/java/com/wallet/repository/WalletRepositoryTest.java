package com.wallet.repository;

import com.wallet.model.User;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();

        testUser = userRepository.save(testUser);

        testWallet = Wallet.builder()
                .user(testUser)
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .address("0x1234567890abcdef")
                .encryptedPrivateKey("encrypted")
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();

        walletRepository.save(testWallet);
    }

    @Test
    void testSaveWallet() {
        Wallet savedWallet = walletRepository.save(testWallet);

        assertNotNull(savedWallet.getId());
        assertEquals("0x1234567890abcdef", savedWallet.getAddress());
    }

    @Test
    void testFindByUserId() {
        List<Wallet> wallets = walletRepository.findByUserId(testUser.getId());

        assertFalse(wallets.isEmpty());
        assertEquals(1, wallets.size());
        assertEquals(testWallet.getAddress(), wallets.get(0).getAddress());
    }

    @Test
    void testFindByAddress() {
        Optional<Wallet> found = walletRepository.findByAddress("0x1234567890abcdef");

        assertTrue(found.isPresent());
        assertEquals(testWallet.getId(), found.get().getId());
    }

    @Test
    void testFindByUserIdAndNetwork() {
        Optional<Wallet> found = walletRepository.findByUserIdAndNetwork(
                testUser.getId(),
                BlockchainNetwork.ETHEREUM_SEPOLIA
        );

        assertTrue(found.isPresent());
        assertEquals(BlockchainNetwork.ETHEREUM_SEPOLIA, found.get().getNetwork());
    }

    @Test
    void testFindByUserIdAndActive() {
        List<Wallet> activeWallets = walletRepository.findByUserIdAndActive(testUser.getId(), true);

        assertEquals(1, activeWallets.size());
        assertTrue(activeWallets.get(0).getActive());
    }

    @Test
    void testExistsByAddress() {
        assertTrue(walletRepository.existsByAddress("0x1234567890abcdef"));
        assertFalse(walletRepository.existsByAddress("0xnonexistent"));
    }
}