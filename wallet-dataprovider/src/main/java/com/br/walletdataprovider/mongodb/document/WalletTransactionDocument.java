package com.br.walletdataprovider.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wallet_transactions")
public class WalletTransactionDocument {
    @Id
    private String id;

    @Indexed
    private String walletId;

    private String type;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String balanceAfterCurrency;
    private String description;

    @Indexed
    private LocalDateTime timestamp;

    private String correlationId;
}
