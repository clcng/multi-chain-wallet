package com.wallet.scheduler;

import com.wallet.model.Transaction;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.service.ScannerService;
import com.wallet.service.TransactionService;
import com.wallet.service.WalletService;
import com.wallet.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionUpdateScheduler {

    private final WalletService walletService;
    private final ScannerService scannerService;
    private final TransactionService transactionService;
    private final Map<BlockchainNetwork, BlockchainService> blockchainServices;

    //TODO:
//    @Scheduled(fixedDelay = 600000)
//    public void updatePendingTransactions() {
//        log.info("Updating pending transactions...");
//        transactionService.updatePendingTransactions();
//    }

    @Scheduled(fixedDelay = 600000)
    public void scanDepositTransactions() {
//        if (!scanner.IsEnabled()) {
//            return;
//        }
        log.debug("Starting deposit scan...");

        for (Map.Entry<BlockchainNetwork, BlockchainService> entry : blockchainServices.entrySet()) {
            entry.getValue().getCurrentBlock();
        }

        List<Wallet> activeWallets = walletService.getActiveWallets();
        log.debug("Scanning {} active wallets", activeWallets.size());

        for (Wallet wallet:activeWallets) {
            try {
                scannerService.scanDeposits(wallet);
            } catch (Exception e) {
                log.error("Error scanning wallet {}: {}", wallet.getAddress(), e.getMessage(), e);
            }
        }


    }

//    @Scheduled(fixedDelay = 600000)
    public void processWithdrawalTransactions() {
//        if (!scanner.IsEnabled()) {
//            return;
//        }

        log.debug("Starting withdrawal scan...");

        transactionService.processApprovedWithdrawTransactions();

//        transactionService.updatePendingTransactions();
        List<Wallet> activeWallets = walletService.getActiveWallets();
        log.debug("Scanning {} active wallets", activeWallets.size());

        for (Wallet wallet:activeWallets) {
            try {
                scannerService.scanDeposits(wallet);
            } catch (Exception e) {
                log.error("Error scanning wallet {}: {}", wallet.getAddress(), e.getMessage(), e);
            }
        }


    }
}
