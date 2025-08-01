package com.br.walletconfig.usecase;

import com.br.walletcore.port.events.EventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import com.br.walletcore.usecase.CreateWalletUseCase;
import com.br.walletcore.usecase.DepositUseCase;
import com.br.walletcore.usecase.GetBalanceUseCase;
import com.br.walletcore.usecase.GetWalletUseCase;
import com.br.walletcore.usecase.TransferUseCase;
import com.br.walletcore.usecase.WithdrawUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableTransactionManagement
public class UseCaseConfig {

    @Bean
    public CreateWalletUseCase createWalletUseCase(WalletRepository walletRepository, EventPublisher eventPublisher) {
        return new CreateWalletUseCase(walletRepository, eventPublisher);
    }

    @Bean
    public GetBalanceUseCase getBalanceUseCase(WalletRepository walletRepository) {
        return new GetBalanceUseCase(walletRepository);
    }

    @Bean
    @Transactional
    public DepositUseCase depositUseCase(WalletRepository walletRepository, EventPublisher eventPublisher) {
        return new DepositUseCase(walletRepository, eventPublisher);
    }

    @Bean
    @Transactional
    public WithdrawUseCase withdrawUseCase(WalletRepository walletRepository, EventPublisher eventPublisher) {
        return new WithdrawUseCase(walletRepository, eventPublisher);
    }

    @Bean
    @Transactional
    public TransferUseCase transferUseCase(WalletRepository walletRepository, EventPublisher eventPublisher) {
        return new TransferUseCase(walletRepository, eventPublisher);
    }

    @Bean
    public GetWalletUseCase getWalletUseCase(WalletRepository walletRepository) {
        return new GetWalletUseCase(walletRepository);
    }
}