package com.wallet.service;

import com.wallet.dto.WalletCreationDto;
import com.wallet.dto.WalletResponseDto;
import com.wallet.model.User;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.repository.WalletRepository;
import com.wallet.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final Map<BlockchainNetwork, BlockchainService> blockchainServices;

    private static final String ENCRYPTION_KEY = "MySecretKey12345";

    @Transactional
    public WalletResponseDto createWallet(WalletCreationDto dto) {
        User user = userService.getCurrentUser();
        if (walletRepository.findByUserIdAndNetwork(user.getId(), dto.getNetwork()).isPresent()) {
            throw new RuntimeException("Wallet already exists for network: " + dto.getNetwork());
        }

        BlockchainService blockchainService = blockchainServices.get(dto.getNetwork());
        if (blockchainService == null) {
            throw new RuntimeException("Blockchain service not available for: " + dto.getNetwork());
        }

        BlockchainService.WalletKeyPair keyPair = blockchainService.generateWallet();
        Wallet wallet = Wallet.builder()
                .user(user)
                .network(dto.getNetwork())
                .address(keyPair.address())
                .encryptedPrivateKey(encryptPrivateKey(keyPair.privateKey()))
                .publicKey(keyPair.publicKey())
                .balance(BigDecimal.ZERO)
                .active(true)
                .isTestnet(true)
                .createdAt(LocalDateTime.now())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created for user {} on network {}: {}",
                user.getUsername(), dto.getNetwork(), savedWallet.getAddress());

        return mapToDto(savedWallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponseDto> getUserWallets() {
        User user = userService.getCurrentUser();
        return walletRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WalletResponseDto getWalletById(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        User currentUser = userService.getCurrentUser();
        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to wallet");
        }

        return mapToDto(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponseDto getWalletByNetwork(BlockchainNetwork network) {
        User currentUser = userService.getCurrentUser();
        Wallet wallet = walletRepository.findByUserIdAndNetwork(currentUser.getId(), network)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to wallet");
        }

        return mapToDto(wallet);
    }

    public List<Wallet> getActiveWallets() {

        return walletRepository.findByActive(true);
    }

    @Transactional
    public Wallet updateWalletBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal currBalance = wallet.getBalance();
        wallet.setBalance(currBalance.add(amount));
        Wallet updatedWallet = walletRepository.save(wallet);

        log.info("Wallet balance updated: {} - {}", wallet.getAddress(), updatedWallet.getBalance());
        return updatedWallet;
    }

    public String decryptPrivateKey(String encryptedPrivateKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPrivateKey));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting private key", e);
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }

    private String encryptPrivateKey(String privateKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(privateKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting private key", e);
            throw new RuntimeException("Failed to encrypt private key", e);
        }
    }

    private WalletResponseDto mapToDto(Wallet wallet) {
        return WalletResponseDto.builder()
                .id(wallet.getId())
                .network(wallet.getNetwork())
                .address(wallet.getAddress())
                .balance(wallet.getBalance())
                .active(wallet.getActive())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    public Wallet getWalletEntity(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public Wallet getWalletEntity(UUID userId, BlockchainNetwork network) {
        return walletRepository.findByUserIdAndNetwork(userId, network)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}
