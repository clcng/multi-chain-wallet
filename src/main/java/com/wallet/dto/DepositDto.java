package com.wallet.dto;

import com.wallet.model.enums.BlockchainNetwork;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositDto {

    @NotNull(message = "Network is required")
    private BlockchainNetwork network;

//    @NotNull(message = "Wallet ID is required")
//    private UUID walletId;

//    @NotBlank(message = "Transaction hash is required")
//    private String transactionHash;
}
