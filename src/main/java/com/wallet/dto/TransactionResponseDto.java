package com.wallet.dto;

import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {
    private UUID id;
    private UUID walletId;
    private TransactionType type;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private BigDecimal fee;
    private TransactionStatus status;
    private String transactionHash;
    private Long blockNumber;
    private Integer confirmations;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}
