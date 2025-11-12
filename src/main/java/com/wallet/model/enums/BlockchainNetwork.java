package com.wallet.model.enums;

public enum BlockchainNetwork {
    ETHEREUM_SEPOLIA("Ethereum Sepolia", "ETH"),
    TRON("Tron", "TRX"),
    TON("TON", "TON");

    private final String displayName;
    private final String nativeCurrency;

    BlockchainNetwork(String displayName, String nativeCurrency) {
        this.displayName = displayName;
        this.nativeCurrency = nativeCurrency;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNativeCurrency() {
        return nativeCurrency;
    }
}
