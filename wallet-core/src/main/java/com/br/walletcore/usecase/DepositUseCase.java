package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.enums.TransactionType;
import com.br.walletcore.port.events.EventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.br.walletcore.enums.WalletEventType.FUNDS_DEPOSITED;

@Slf4j
@RequiredArgsConstructor
public class DepositUseCase {

    private static final String DESCRIPTION_TRANSACTION = "Deposit to wallet";

    private final WalletRepository walletRepository;
    private final EventPublisher eventPublisher;

    public WalletTransaction deposit(String userId, Money amount) {
        log.info("Processing deposit for user: {}, amount: {}", userId, amount);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        Money previousBalance = wallet.getBalance();
        wallet.deposit(amount);

        Wallet updatedWallet = walletRepository.save(wallet);

        var transaction = WalletTransaction.builder()
                .id(UUID.randomUUID().toString())
                .walletId(wallet.getId())
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .balanceAfter(updatedWallet.getBalance())
                .description(DESCRIPTION_TRANSACTION)
                .timestamp(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();

        walletRepository.saveTransaction(transaction);

        eventPublisher.publishWalletEvent(FUNDS_DEPOSITED.getName(), Map.of(
                "walletId", wallet.getId(),
                "userId", wallet.getUserId(),
                "amount", amount.getAmount(),
                "currency", amount.getCurrency(),
                "previousBalance", previousBalance.getAmount(),
                "newBalance", updatedWallet.getBalance().getAmount(),
                "timestamp", transaction.getTimestamp().toString(),
                "transactionId", transaction.getId()
        ));

        log.info("Deposit completed for user: {}", userId);
        return transaction;
    }
}
