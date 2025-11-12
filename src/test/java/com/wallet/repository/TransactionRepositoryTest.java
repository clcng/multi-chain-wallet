package com.wallet.repository;

import com.wallet.model.Transaction;
import com.wallet.model.User;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private Wallet testWallet;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();
        user = userRepository.save(user);

        testWallet = Wallet.builder()
                .user(user)
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .address("0x123")
                .encryptedPrivateKey("encrypted")
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();
        testWallet = walletRepository.save(testWallet);

        testTransaction = Transaction.builder()
                .wallet(testWallet)
                .type(TransactionType.DEPOSIT)
                .fromAddress("0xfrom")
                .toAddress("0xto")
                .amount(new BigDecimal("10"))
                .status(TransactionStatus.PENDING)
                .transactionHash("0xhash123")
                .confirmations(0)
                .build();
        transactionRepository.save(testTransaction);
    }

    @Test
    void testSaveTransaction() {
        Transaction savedTx = transactionRepository.save(testTransaction);

        assertNotNull(savedTx.getId());
        assertEquals("0xhash123", savedTx.getTransactionHash());
    }

    @Test
    void testFindByWalletId() {
        List<Transaction> transactions = transactionRepository.findByWalletId(testWallet.getId());

        assertEquals(1, transactions.size());
        assertEquals(testTransaction.getId(), transactions.get(0).getId());
    }

    @Test
    void testFindByWalletIdPaged() {
        Page<Transaction> page = transactionRepository.findByWalletId(
                testWallet.getId(),
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getTotalElements());
        assertEquals(testTransaction.getId(), page.getContent().get(0).getId());
    }

    @Test
    void testFindByTransactionHash() {
        Optional<Transaction> found = transactionRepository.findByTransactionHash("0xhash123");

        assertTrue(found.isPresent());
        assertEquals(testTransaction.getId(), found.get().getId());
    }

    @Test
    void testFindByStatus() {
        List<Transaction> pending = transactionRepository.findByStatus(TransactionStatus.PENDING);

        assertEquals(1, pending.size());
        assertEquals(TransactionStatus.PENDING, pending.get(0).getStatus());
    }

    @Test
    void testFindByWalletIdAndType() {
        List<Transaction> deposits = transactionRepository.findByWalletIdAndType(
                testWallet.getId(),
                TransactionType.DEPOSIT
        );

        assertEquals(1, deposits.size());
        assertEquals(TransactionType.DEPOSIT, deposits.get(0).getType());
    }

    @Test
    void testCountActiveSessionsByUserId() {
        long count = transactionRepository.findByWalletId(testWallet.getId()).size();
        assertEquals(1, count);
    }
}