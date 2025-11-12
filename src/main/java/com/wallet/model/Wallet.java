package com.wallet.model;

import com.wallet.model.enums.BlockchainNetwork;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_address", columnList = "address", unique = true),
    @Index(name = "idx_wallet_user_network", columnList = "user_id, network")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "transactions"}) // Exclude lazy fields from toString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BlockchainNetwork network;

    @Column(name = "is_testnet", nullable = false)
    private Boolean isTestnet;

    @Column(nullable = false, unique = true, length = 255)
    private String address;

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(nullable = false, precision = 36, scale = 18)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @Column(name = "last_scanned_lt", nullable = false)
    private Long lastScannedLt = 0L;

    @Column(name = "last_scanned_hash", nullable = false)
    private String lastScannedHash = "";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setWallet(this);
    }
}
