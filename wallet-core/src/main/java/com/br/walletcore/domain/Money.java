package com.br.walletcore.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Value
@Builder
public class Money {
    BigDecimal amount;
    String currency;

    public static Money of(BigDecimal amount, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return Money.builder()
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .currency(currency.toUpperCase())
                .build();
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return Money.of(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        return Money.of(result, this.currency);
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }
}