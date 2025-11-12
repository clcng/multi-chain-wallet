package com.wallet.service;

import com.wallet.dto.TransactionResponseDto;
import com.wallet.dto.WithdrawApproveDto;
import com.wallet.dto.WithdrawalDto;
import com.wallet.model.Transaction;
import com.wallet.model.User;
import com.wallet.model.Wallet;
import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final UserService userService;
    private final Map<com.wallet.model.enums.BlockchainNetwork, BlockchainService> blockchainServices;

    @Transactional
    public TransactionResponseDto processWithdrawal(WithdrawalDto withdrawalDto) {

        User currentUser = userService.getCurrentUser();
        Wallet wallet = walletRepository.findByUserIdAndNetwork(currentUser.getId(), withdrawalDto.getNetwork())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (wallet.getBalance().compareTo(withdrawalDto.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // hold balance to avoid over withdraw
        wallet.setBalance(wallet.getBalance().subtract(withdrawalDto.getAmount()));

        BlockchainService blockchainService = blockchainServices.get(wallet.getNetwork());
        if (!blockchainService.isValidAddress(withdrawalDto.getToAddress())) {
            throw new RuntimeException("Invalid destination address");
        }

        Transaction transaction = Transaction.builder()
                .userId(wallet.getUser().getId())
                .wallet(wallet)
                .type(TransactionType.WITHDRAWAL)
                .fromAddress(wallet.getAddress())
                .toAddress(withdrawalDto.getToAddress())
                .amount(withdrawalDto.getAmount())
//                .fee(txDetails.fee())
                .status(TransactionStatus.PENDING)
//                .transactionHash(depositDto.getTransactionHash())
//                .blockNumber(txDetails.blockNumber())
//                .confirmations(txDetails.confirmations())
                .memo(withdrawalDto.getMemo())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        walletRepository.save(wallet);

        log.info("Withdrawal initiated: {} - Amount: {}", savedTransaction.getId(), withdrawalDto.getAmount());
        return mapToDto(savedTransaction);
    }

    public TransactionResponseDto withdrawApprove(WithdrawApproveDto withdrawApproveDto) {
        Transaction transaction = transactionRepository.findById(withdrawApproveDto.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (transaction.getAmount().compareTo(withdrawApproveDto.getAmount()) != 0 ||
            transaction.getToAddress().equalsIgnoreCase(withdrawApproveDto.getToAddress())) {
            throw new IllegalArgumentException("transaction info mismatch");
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transactionRepository.save(transaction);

        return mapToDto(transaction);
    }

    public void processApprovedWithdrawTransactions() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.APPROVED);
        for (Transaction transaction:transactions) {
            if (transaction.getType() != TransactionType.WITHDRAWAL) {
                continue;
            }

            processWithdrawalAsync(transaction.getId());

        }
    }

    @Async
    @Transactional
    public void processWithdrawalAsync(UUID transactionId) {
        try {
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            Wallet wallet = transaction.getWallet();
            BlockchainService blockchainService = blockchainServices.get(wallet.getNetwork());

            String privateKey = walletService.decryptPrivateKey(wallet.getEncryptedPrivateKey());
            String txHash = blockchainService.sendTransaction(
                    privateKey,
                    transaction.getToAddress(),
                    transaction.getAmount()
            );

            transaction.setTransactionHash(txHash);
            transaction.setStatus(TransactionStatus.CONFIRMED);
            transaction.setConfirmedAt(LocalDateTime.now());

            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));

            transactionRepository.save(transaction);

            log.info("Withdrawal completed: {} - TxHash: {}", transactionId, txHash);
        } catch (Exception e) {
            log.error("Error processing withdrawal: {}", transactionId, e);

            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
            if (transaction != null) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setErrorMessage(e.getMessage());
                transactionRepository.save(transaction);

                walletService.updateWalletBalance(transaction.getWallet().getId(), transaction.getAmount());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getWalletTransactions(UUID walletId) {
        Wallet wallet = walletService.getWalletEntity(walletId);

        User currentUser = userService.getCurrentUser();
        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to wallet");
        }

        return transactionRepository.findByWalletId(walletId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getWalletTransactionsPaged(UUID walletId, Pageable pageable) {
        Wallet wallet = walletService.getWalletEntity(walletId);

        User currentUser = userService.getCurrentUser();
        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to wallet");
        }

        return transactionRepository.findByWalletId(walletId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        User currentUser = userService.getCurrentUser();
        if (!transaction.getWallet().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to transaction");
        }

        return mapToDto(transaction);
    }


    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getUserTransactions(UUID userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        List<TransactionResponseDto> dtos = new ArrayList<>();
        for (Transaction transaction:transactions) {
            dtos.add(mapToDto(transaction));
        }
        return dtos;
    }

    @Async
    @Transactional
    public void updatePendingTransactions() {
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(TransactionStatus.PENDING);

        for (Transaction transaction : pendingTransactions) {
            try {
                BlockchainService blockchainService =
                        blockchainServices.get(transaction.getWallet().getNetwork());

                BlockchainService.TransactionDetails txDetails =
                        blockchainService.getTransactionDetails(transaction.getTransactionHash());

                transaction.setBlockNumber(txDetails.blockNumber());
                transaction.setConfirmations(txDetails.confirmations());

                if (txDetails.isConfirmed() && transaction.getStatus() == TransactionStatus.PENDING) {
                    transaction.setStatus(TransactionStatus.CONFIRMED);
                    transaction.setConfirmedAt(LocalDateTime.now());

                    if (transaction.getType() == TransactionType.DEPOSIT) {
                        Wallet wallet = transaction.getWallet();
                        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
                    }
                }

                //TODO: need update wallet?
                transactionRepository.save(transaction);
            } catch (Exception e) {
                log.error("Error updating transaction: {}", transaction.getId(), e);
            }
        }
    }

    @Transactional
    public void processDeposit(Wallet wallet, Transaction transaction) {
        log.info("Processing deposit: {} TON from {} to {}",
                transaction.getAmount(), transaction.getFromAddress(), wallet.getAddress());

        transaction.setType(TransactionType.DEPOSIT);
        transaction.setWallet(wallet);
        transaction.setUserId(wallet.getUser().getId());
        transactionRepository.save(transaction);

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            walletService.updateWalletBalance(transaction.getWallet().getId(), transaction.getAmount());
        }
    }

    private TransactionResponseDto mapToDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .type(transaction.getType())
                .fromAddress(transaction.getFromAddress())
                .toAddress(transaction.getToAddress())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .status(transaction.getStatus())
                .transactionHash(transaction.getTransactionHash())
                .blockNumber(transaction.getBlockNumber())
                .confirmations(transaction.getConfirmations())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .confirmedAt(transaction.getConfirmedAt())
                .build();
    }
}
