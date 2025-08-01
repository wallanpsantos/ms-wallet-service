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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.br.walletcore.enums.WalletEventType.FUNDS_TRANSFERRED;

@Slf4j
@RequiredArgsConstructor
public class TransferUseCase {

    private final WalletRepository walletRepository;
    private final WalletEventPublisher walletEventPublisher;
    private final OutboxEventPublisher outboxEventPublisher;

    public List<WalletTransaction> transfer(String fromUserId, String toUserId, Money amount) {
        String correlationId = UUID.randomUUID().toString();
        log.info("Processing transfer from: {} to: {}, amount: {}, correlationId: {}",
                fromUserId, toUserId, amount, correlationId);

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot transfer to same user");
        }

        Wallet sourceWallet = walletRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found"));

        Wallet targetWallet = walletRepository.findByUserId(toUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target wallet not found"));

        if (!sourceWallet.getBalance().getCurrency().equals(targetWallet.getBalance().getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch between wallets");
        }

        // Processar retirada da carteira origem
        Money sourceBalance = sourceWallet.getBalance();
        sourceWallet.withdraw(amount);
        Wallet updatedSourceWallet = walletRepository.save(sourceWallet);

        var withdrawTransaction = WalletTransaction.builder()
                .id(UUID.randomUUID().toString())
                .walletId(sourceWallet.getId())
                .type(TransactionType.TRANSFER_OUT)
                .amount(amount)
                .balanceAfter(updatedSourceWallet.getBalance())
                .description("Transfer to user: " + toUserId)
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        walletRepository.saveTransaction(withdrawTransaction);

        // Processar depósito na carteira destino
        Money targetBalance = targetWallet.getBalance();
        targetWallet.deposit(amount);
        Wallet updatedTargetWallet = walletRepository.save(targetWallet);

        WalletTransaction depositTransaction = WalletTransaction.builder()
                .id(UUID.randomUUID().toString())
                .walletId(targetWallet.getId())
                .type(TransactionType.TRANSFER_IN)
                .amount(amount)
                .balanceAfter(updatedTargetWallet.getBalance())
                .description("Transfer from user: " + fromUserId)
                .timestamp(LocalDateTime.now())
                .correlationId(correlationId)
                .build();

        walletRepository.saveTransaction(depositTransaction);

        walletEventPublisher.publishWalletEvent(FUNDS_TRANSFERRED.getName(),
                getPayload(fromUserId, toUserId, amount, correlationId, sourceWallet, targetWallet, sourceBalance, updatedSourceWallet, targetBalance, updatedTargetWallet, depositTransaction));

        outboxEventPublisher.publishOutboxEvent(FUNDS_TRANSFERRED.getName(),
                getPayload(fromUserId, toUserId, amount, correlationId, sourceWallet, targetWallet, sourceBalance, updatedSourceWallet, targetBalance, updatedTargetWallet, depositTransaction));

        log.info("Transfer completed successfully from {} to {}", fromUserId, toUserId);
        return List.of(withdrawTransaction, depositTransaction);
    }

    private static Map<String, ? extends Serializable> getPayload(String fromUserId,
                                                                  String toUserId,
                                                                  Money amount,
                                                                  String correlationId,
                                                                  Wallet sourceWallet,
                                                                  Wallet targetWallet,
                                                                  Money sourceBalance,
                                                                  Wallet updatedSourceWallet,
                                                                  Money targetBalance,
                                                                  Wallet updatedTargetWallet,
                                                                  WalletTransaction depositTransaction) {
        return Map.ofEntries(
                Map.entry("correlationId", correlationId),
                Map.entry("sourceWalletId", sourceWallet.getId()),
                Map.entry("targetWalletId", targetWallet.getId()),
                Map.entry("sourceUserId", fromUserId),
                Map.entry("targetUserId", toUserId),
                Map.entry("amount", amount.getAmount()),
                Map.entry("currency", amount.getCurrency()),
                Map.entry("sourceBalanceBefore", sourceBalance.getAmount()),
                Map.entry("sourceBalanceAfter", updatedSourceWallet.getBalance().getAmount()),
                Map.entry("targetBalanceBefore", targetBalance.getAmount()),
                Map.entry("targetBalanceAfter", updatedTargetWallet.getBalance().getAmount()),
                Map.entry("timestamp", depositTransaction.getTimestamp().toString())
        );
    }
}
