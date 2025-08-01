package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.port.events.EventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static com.br.walletcore.enums.WalletEventType.WALLET_CREATED;

@Slf4j
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletRepository walletRepository;
    private final EventPublisher eventPublisher;

    public Wallet createWallet(String userId, String currency) {
        log.info("Creating wallet for user: {}", userId);

        walletRepository.findByUserId(userId).ifPresent(w -> {
            throw new IllegalArgumentException("User already has a wallet");
        });

        var wallet = Wallet.builder()
                .userId(userId)
                .balance(Money.of(BigDecimal.ZERO, currency))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        eventPublisher.publishWalletEvent(WALLET_CREATED.getName(), Map.of(
                "walletId", savedWallet.getId(),
                "userId", savedWallet.getUserId(),
                "currency", savedWallet.getBalance().getCurrency(),
                "timestamp", savedWallet.getCreatedAt()
        ));

        log.info("Wallet created successfully for user: {}", userId);
        return savedWallet;
    }
}
