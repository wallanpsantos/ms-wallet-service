package com.br.walletcore.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    private String id;
    private String userId;
    private Money balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public void deposit(Money amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(Money amount) {
        validateAmount(amount);
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    private void validateAmount(Money amount) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (!this.balance.getCurrency().equals(amount.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }
}