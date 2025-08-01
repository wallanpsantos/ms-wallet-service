package com.br.walletcore.port.repositories;

import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findByUserId(String userId);

    List<WalletTransaction> findTransactionsByWalletIdUntilTimestamp(String walletId, LocalDateTime timestamp);

    void saveTransaction(WalletTransaction transaction);
}
