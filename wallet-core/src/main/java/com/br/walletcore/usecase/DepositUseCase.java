package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.enums.TransactionType;
import com.br.walletcore.port.events.OutboxEventPublisher;
import com.br.walletcore.port.events.WalletEventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.br.walletcore.enums.WalletEventType.FUNDS_DEPOSITED;
import static com.br.walletcore.enums.WalletEventType.WALLET_CREATED;

@Slf4j
@RequiredArgsConstructor
public class DepositUseCase {

    private static final String DESCRIPTION_TRANSACTION = "Deposit to wallet";

    private final WalletRepository walletRepository;
    private final WalletEventPublisher walletEventPublisher;
    private final OutboxEventPublisher outboxEventPublisher;

    public WalletTransaction execute(String userId, Money amount) {
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

        walletEventPublisher.publishWalletEvent(FUNDS_DEPOSITED.getName(), getPayload(amount, wallet, previousBalance, updatedWallet, transaction));

        outboxEventPublisher.publishOutboxEvent(WALLET_CREATED.getName(), getPayload(amount, wallet, previousBalance, updatedWallet, transaction));

        log.info("Deposit completed for user: {}", userId);
        return transaction;
    }

    private static Map<String, ? extends Serializable> getPayload(Money amount,
                                                                  Wallet wallet,
                                                                  Money previousBalance,
                                                                  Wallet updatedWallet,
                                                                  WalletTransaction transaction) {
        return Map.of(
                "walletId", wallet.getId(),
                "userId", wallet.getUserId(),
                "amount", amount.getAmount(),
                "currency", amount.getCurrency(),
                "previousBalance", previousBalance.getAmount(),
                "newBalance", updatedWallet.getBalance().getAmount(),
                "timestamp", transaction.getTimestamp().toString(),
                "transactionId", transaction.getId()
        );
    }
}
