package com.br.walletcore.enums;

import lombok.Getter;

@Getter
public enum CurrencyType {

    BRL("BRL", "Brazilian Real"),
    USD("USD", "United States Dollar"),
    CAD("CAD", "Canadian Dollar"),
    EUR("EUR", "Euro"),
    ARS("ARS", "Argentine Peso");

    private final String currency;
    private final String description;

    CurrencyType(String currency, String description) {
        this.currency = currency;
        this.description = description;
    }

    public static boolean existCurrency(String acronym) {
        if (acronym == null || acronym.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency acronym cannot be null or empty");
        }

        for (CurrencyType currency : CurrencyType.values()) {
            if (currency.getCurrency().equalsIgnoreCase(acronym.trim())) {
                return true;
            }
        }

        throw new IllegalArgumentException("Moeda com sigla '" + acronym + "' not found!");
    }

}
