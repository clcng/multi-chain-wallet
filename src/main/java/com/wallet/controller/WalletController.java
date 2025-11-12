package com.wallet.controller;

import com.wallet.dto.WalletCreationDto;
import com.wallet.dto.WalletResponseDto;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<?> createWallet(@Valid @RequestBody WalletCreationDto walletDto) {
        try {
            WalletResponseDto wallet = walletService.createWallet(walletDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<WalletResponseDto>> getUserWallets() {
        List<WalletResponseDto> wallets = walletService.getUserWallets();
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{network}")
    public ResponseEntity<?> getWallet(@PathVariable BlockchainNetwork network) {
        try {
            WalletResponseDto wallet = walletService.getWalletByNetwork(network);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/deposit-address/{network}")
    public ResponseEntity<?> getDepositAddress(@PathVariable BlockchainNetwork network) {
        try {

            WalletResponseDto walletDto = walletService.getWalletByNetwork(network);
            Map<String, String> response = new HashMap<>();
            response.put("address", walletDto.getAddress());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
//            log.error("Error retrieving deposit address: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve deposit address"));
        }
    }

    /*
    @PutMapping("/{walletId}/balance")
    public ResponseEntity<?> updateBalance(@PathVariable UUID walletId) {
        try {
            WalletResponseDto wallet = walletService.updateWalletBalance(walletId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    */
}
