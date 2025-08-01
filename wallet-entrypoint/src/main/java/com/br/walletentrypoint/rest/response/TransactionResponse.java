package com.br.walletentrypoint.rest.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(String id,
                                  String walletId,
                                  String type,
                                  BigDecimal amount,
                                  String currency,
                                  BigDecimal balanceAfter,
                                  LocalDateTime timestamp,
                                  String correlationId
) {
}
