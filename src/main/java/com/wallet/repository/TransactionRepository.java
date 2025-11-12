package com.wallet.repository;

import com.wallet.model.Transaction;
import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByWalletId(UUID walletId);

    Page<Transaction> findByWalletId(UUID walletId, Pageable pageable);

    List<Transaction> findByUserId(UUID userId);

    Optional<Transaction> findByTransactionHash(String transactionHash);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByWalletIdAndType(UUID walletId, TransactionType type);

    List<Transaction> findByWalletIdAndStatus(UUID walletId, TransactionStatus status);
}
