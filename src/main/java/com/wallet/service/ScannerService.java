package com.wallet.service;

import com.wallet.model.Transaction;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final Map<BlockchainNetwork, BlockchainService> blockchainServices;

    /**
     * Scan deposits for specific wallet
     */
    @Async
    @Transactional
    public void scanDeposits(Wallet wallet) {
        log.debug("Scanning deposits for wallet: {}", wallet.getAddress());

        BlockchainService blockchainService = blockchainServices.get(wallet.getNetwork());
        List<Transaction> transactions = blockchainService.getWalletTransactions(wallet.getAddress(), wallet.getLastScannedLt(), wallet.getLastScannedHash(), 100);
        if (transactions.isEmpty()) {
            log.debug("No new transactions for wallet: {}", wallet.getAddress());
            return;
        }

        log.info("Found {} transactions for wallet {}", transactions.size(), wallet.getAddress());
        Long maxLt = wallet.getLastScannedLt();
        String lastHash = wallet.getLastScannedHash();
        for (Transaction tx : transactions) {
            try {
                // Check if transaction is incoming
                if (!tx.getToAddress().equalsIgnoreCase(wallet.getAddress())) {
                    continue;
                }
                if (tx.getBlockNumber() < wallet.getLastScannedLt()) {
                    continue;
                }
                // Check if already processed
                if (transactionRepository.findByTransactionHash(tx.getTransactionHash()).isPresent()) {
                    continue;
                }

                transactionService.processDeposit(wallet, tx);

                // Update max logical time
                if (tx.getBlockNumber() > maxLt) {
                    maxLt = tx.getBlockNumber();
                }
                lastHash = tx.getTransactionHash();

            } catch (Exception e) {
                log.error("Error processing transaction: {}", e.getMessage(), e);
            }
        }

        // Update last scanned logical time
        wallet.setLastScannedLt(maxLt);
        wallet.setLastScannedHash(lastHash);
        walletRepository.save(wallet);
    }

}
