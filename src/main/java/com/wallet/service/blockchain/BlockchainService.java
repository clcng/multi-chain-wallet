package com.wallet.service.blockchain;

import com.wallet.model.Transaction;
import com.wallet.model.enums.BlockchainNetwork;

import java.math.BigDecimal;
import java.util.List;

public interface BlockchainService {

    BlockchainNetwork getNetwork();

    WalletKeyPair generateWallet();

    BigDecimal getBalance(String address);

    String sendTransaction(String privateKey, String toAddress, BigDecimal amount);

    List<Transaction> getWalletTransactions(String address, Long fromLt, String fromHash, int limit);

    TransactionDetails getTransactionDetails(String transactionHash);

    boolean isValidAddress(String address);

    void getCurrentBlock();

    record WalletKeyPair(String address, String privateKey, String publicKey) {}

    record TransactionDetails(
            String hash,
            String from,
            String to,
            BigDecimal amount,
            BigDecimal fee,
            Long blockNumber,
            Integer confirmations,
            boolean isConfirmed
    ) {}
}
