package com.br.walletcore.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAW"),
    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

}