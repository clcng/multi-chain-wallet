package com.wallet.dto;

import com.wallet.model.enums.BlockchainNetwork;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletCreationDto {

    @NotNull(message = "Network is required")
    private BlockchainNetwork network;
}
