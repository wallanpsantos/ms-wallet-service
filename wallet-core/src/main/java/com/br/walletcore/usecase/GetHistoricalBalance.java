package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.enums.CurrencyType;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GetHistoricalBalance {

    private final WalletRepository walletRepository;

    public Money execute(String userId, LocalDate date) {
        log.info("Getting historical balance for user: {} at date: {}", userId, date);

        validateInputs(userId, date);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<WalletTransaction> transactions = walletRepository
                .findTransactionsByWalletIdUntilTimestamp(wallet.getId(), endOfDay);

        return transactions.stream()
                .max(Comparator.comparing(WalletTransaction::getTimestamp))
                .map(WalletTransaction::getBalanceAfter)
                .orElse(createZeroBalance(wallet));
    }


    private void validateInputs(String userId, LocalDate date) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }
    }

    private Money createZeroBalance(Wallet wallet) {
        if (wallet.getBalance() != null) {
            return Money.of(BigDecimal.ZERO, wallet.getBalance().getCurrency());
        }

        log.warn("Wallet balance is null for wallet: {}, using default currency {}", wallet.getId(), CurrencyType.BRL);
        return Money.of(BigDecimal.ZERO, CurrencyType.BRL.getCurrency());
    }

}
