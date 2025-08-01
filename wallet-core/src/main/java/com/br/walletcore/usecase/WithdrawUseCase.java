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

import static com.br.walletcore.enums.WalletEventType.FUNDS_WITHDRAWN;

@Slf4j
@RequiredArgsConstructor
public class WithdrawUseCase {

    private final WalletRepository walletRepository;
    private final WalletEventPublisher walletEventPublisher;
    private final OutboxEventPublisher outboxEventPublisher;

    public WalletTransaction withdraw(String userId, Money amount) {
        log.info("Processing withdrawal for user: {}, amount: {}", userId, amount);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        Money previousBalance = wallet.getBalance();
        wallet.withdraw(amount);

        Wallet updatedWallet = walletRepository.save(wallet);

        var transaction = WalletTransaction.builder()
                .id(UUID.randomUUID().toString())
                .walletId(wallet.getId())
                .type(TransactionType.WITHDRAW)
                .amount(amount)
                .balanceAfter(updatedWallet.getBalance())
                .description("Withdrawal from wallet")
                .timestamp(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();

        walletRepository.saveTransaction(transaction);

        walletEventPublisher.publishWalletEvent(FUNDS_WITHDRAWN.getName(),
                getPayload(amount, wallet, previousBalance, updatedWallet, transaction));

        outboxEventPublisher.publishOutboxEvent(FUNDS_WITHDRAWN.getName(),
                getPayload(amount, wallet, previousBalance, updatedWallet, transaction));

        log.info("Withdrawal completed for user: {}", userId);
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
