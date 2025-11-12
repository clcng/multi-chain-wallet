package com.wallet.service;

import com.wallet.dto.WalletCreationDto;
import com.wallet.dto.WalletResponseDto;
import com.wallet.model.User;
import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.repository.WalletRepository;
import com.wallet.service.blockchain.BlockchainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserService userService;

    @Mock
    private Map<BlockchainNetwork, BlockchainService> blockchainServices;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .build();

        testWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .address("0x1234567890abcdef")
                .encryptedPrivateKey("encrypted")
                .balance(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    @Test
    void testCreateWallet_Success() {
        WalletCreationDto dto = WalletCreationDto.builder()
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .build();

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.findByUserIdAndNetwork(testUser.getId(), BlockchainNetwork.ETHEREUM_SEPOLIA))
                .thenReturn(Optional.empty());
        when(blockchainServices.get(BlockchainNetwork.ETHEREUM_SEPOLIA)).thenReturn(blockchainService);
        when(blockchainService.generateWallet()).thenReturn(
                new BlockchainService.WalletKeyPair("0x123", "privateKey", "publicKey")
        );
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        WalletResponseDto result = walletService.createWallet(dto);

        assertNotNull(result);
        assertEquals(BlockchainNetwork.ETHEREUM_SEPOLIA, result.getNetwork());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testCreateWallet_AlreadyExists() {
        WalletCreationDto dto = WalletCreationDto.builder()
                .network(BlockchainNetwork.ETHEREUM_SEPOLIA)
                .build();

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.findByUserIdAndNetwork(testUser.getId(), BlockchainNetwork.ETHEREUM_SEPOLIA))
                .thenReturn(Optional.of(testWallet));

        assertThrows(RuntimeException.class, () -> walletService.createWallet(dto));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testGetUserWallets() {
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(walletRepository.findByUserId(testUser.getId()))
                .thenReturn(Collections.singletonList(testWallet));

        var result = walletService.getUserWallets();

        assertEquals(1, result.size());
        assertEquals(testWallet.getAddress(), result.get(0).getAddress());
    }

    @Test
    void testGetWalletById_Success() {
        UUID walletId = testWallet.getId();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(userService.getCurrentUser()).thenReturn(testUser);

        WalletResponseDto result = walletService.getWalletById(walletId);

        assertNotNull(result);
        assertEquals(testWallet.getAddress(), result.getAddress());
    }

}