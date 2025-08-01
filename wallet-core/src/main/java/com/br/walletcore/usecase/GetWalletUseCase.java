package com.br.walletcore.usecase;

import com.br.walletcore.domain.Wallet;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GetWalletUseCase {

    private final WalletRepository walletRepository;

    public Wallet getWalletByUserId(String userId) {
        log.info("Getting wallet for user: {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }

        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
    }
}

