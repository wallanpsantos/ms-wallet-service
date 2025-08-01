package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
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
public class GetBalanceUseCase {

    private static final String CURRENCY_DEFAULT = "BRL";

    private final WalletRepository walletRepository;

    public Money getCurrentBalance(String userId) {
        log.info("Getting current balance for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        return wallet.getBalance();
    }

    public Money getHistoricalBalance(String userId, LocalDate date) {
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

    private void validatePeriodInputs(String userId, LocalDate startDate, LocalDate endDate) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }

    private Money createZeroBalance(Wallet wallet) {
        if (wallet.getBalance() != null) {
            return Money.of(BigDecimal.ZERO, wallet.getBalance().getCurrency());
        }

        log.warn("Wallet balance is null for wallet: {}, using default currency {}", wallet.getId(), CURRENCY_DEFAULT);
        return Money.of(BigDecimal.ZERO, CURRENCY_DEFAULT);
    }
}