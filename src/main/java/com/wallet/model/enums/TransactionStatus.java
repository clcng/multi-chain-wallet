package com.wallet.model.enums;

public enum TransactionStatus {
    PENDING,
    CONFIRMED,
    SUCCESS,
    FAILED,
    APPROVED,
    CANCELLED,
    PROCESSING, // For withdrawals
    SENT,       // Withdrawal sent
    REJECTED    // Withdrawal rejected
}
