package com.br.walletentrypoint.rest.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(String id,
                             String userId,
                             BigDecimal balance,
                             String currency,
                             LocalDateTime createdAt
) {
}
