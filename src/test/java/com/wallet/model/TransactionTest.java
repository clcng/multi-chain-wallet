package com.wallet.model;

import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .fromAddress("0xfrom")
                .toAddress("0xto")
                .amount(new BigDecimal("10.5"))
                .fee(new BigDecimal("0.001"))
                .status(TransactionStatus.PENDING)
                .transactionHash("0xhash123")
                .confirmations(0)
                .build();
    }

    @Test
    void testTransactionCreation() {
        assertNotNull(transaction);
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(new BigDecimal("10.5"), transaction.getAmount());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertEquals(0, transaction.getConfirmations());
    }

    @Test
    void testTransactionBuilder() {
        Transaction tx = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("5"))
                .build();

        assertEquals(TransactionType.WITHDRAWAL, tx.getType());
        assertEquals(TransactionStatus.PENDING, tx.getStatus());
    }
}