package com.wallet.controller;

import com.wallet.dto.TransactionResponseDto;
import com.wallet.dto.WithdrawApproveDto;
import com.wallet.dto.WithdrawalDto;
import com.wallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawalDto withdrawalDto) {
        // check balance -> create txn and save to db -> approve(?) -> payout proc

        try {
            TransactionResponseDto transaction = transactionService.processWithdrawal(withdrawalDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/withdraw/approve")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawApproveDto withdrawApproveDto) {
        TransactionResponseDto transactionResponseDto = transactionService.withdrawApprove(withdrawApproveDto);
        return ResponseEntity.ok(transactionResponseDto);
    }

    @GetMapping("/list/user/{userId}")
    public ResponseEntity<?> getUserTransactions(@PathVariable UUID userId) {
        try {
            List<TransactionResponseDto> transactions = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving transactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","Failed to retrieve transactions"));
        }
    }

    @GetMapping("/list/{walletId}")
    public ResponseEntity<List<TransactionResponseDto>> getWalletTransactions(
            @PathVariable UUID walletId) {
        List<TransactionResponseDto> transactions = transactionService.getWalletTransactions(walletId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/list/{walletId}/paged")
    public ResponseEntity<Page<TransactionResponseDto>> getWalletTransactionsPaged(
            @PathVariable UUID walletId,
            Pageable pageable) {
        Page<TransactionResponseDto> transactions =
                transactionService.getWalletTransactionsPaged(walletId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("info/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable UUID transactionId) {
        try {
            TransactionResponseDto transaction = transactionService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
