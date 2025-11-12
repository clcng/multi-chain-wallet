package com.wallet.repository;

import com.wallet.model.Wallet;
import com.wallet.model.enums.BlockchainNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByAddress(String address);

    Optional<Wallet> findByUserIdAndNetwork(UUID userId, BlockchainNetwork network);

    List<Wallet> findByActive(Boolean active);

    List<Wallet> findByUserIdAndActive(UUID userId, Boolean active);

    Boolean existsByAddress(String address);
}
