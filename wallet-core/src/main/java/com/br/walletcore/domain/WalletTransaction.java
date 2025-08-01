package com.br.walletcore.domain;

import com.br.walletcore.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    private String id;
    private String walletId;
    private TransactionType type;
    private Money amount;
    private Money balanceAfter;
    private String description;
    private LocalDateTime timestamp;
    private String correlationId;
}