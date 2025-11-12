package com.wallet.dto;

import com.wallet.model.enums.BlockchainNetwork;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalDto {

    @NotBlank(message = "Network is required")
    private BlockchainNetwork network;

    @NotBlank(message = "Destination address is required")
    private String toAddress;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String memo;
}
