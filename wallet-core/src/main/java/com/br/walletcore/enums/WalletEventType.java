package com.br.walletcore.enums;

import lombok.Getter;

@Getter
public enum WalletEventType {
    FUNDS_DEPOSITED("FUNDS_DEPOSITED"),
    FUNDS_WITHDRAWN("FUNDS_WITHDRAWN"),
    FUNDS_TRANSFERRED("FUNDS_TRANSFERRED"),
    WALLET_CREATED("WALLET_CREATED");

    private final String name;

    WalletEventType(String name) {
        this.name = name;
    }
}
