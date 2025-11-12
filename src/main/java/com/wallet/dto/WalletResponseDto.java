package com.wallet.dto;

import com.wallet.model.enums.BlockchainNetwork;
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
public class WalletResponseDto {
    private UUID id;
    private BlockchainNetwork network;
    private String address;
    private BigDecimal balance;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
