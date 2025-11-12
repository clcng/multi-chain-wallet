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
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawApproveDto {

    @NotBlank(message = "Transaction ID is required")
    private UUID transactionId;

    private String toAddress;

    private BigDecimal amount;

}
