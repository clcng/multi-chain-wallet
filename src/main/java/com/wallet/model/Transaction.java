package com.wallet.model;

import com.wallet.model.enums.TransactionStatus;
import com.wallet.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_hash", columnList = "transaction_hash"),
    @Index(name = "idx_transaction_wallet", columnList = "wallet_id"),
    @Index(name = "idx_transaction_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"wallet"}) // Exclude lazy fields
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    @Column(nullable = false, precision = 36, scale = 18)
    private BigDecimal amount;

    @Column(precision = 36, scale = 18)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_hash", length = 255)
    private String transactionHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "confirmations")
    @Builder.Default
    private Integer confirmations = 0;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
}
