package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.port.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final WalletRepository walletRepository;

    public Money execute(String userId) {
        log.info("Getting current balance for user: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        return wallet.getBalance();
    }


}