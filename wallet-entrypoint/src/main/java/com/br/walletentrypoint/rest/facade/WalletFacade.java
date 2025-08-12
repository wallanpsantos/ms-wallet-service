package com.br.walletentrypoint.rest.facade;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.usecase.CreateWalletUseCase;
import com.br.walletcore.usecase.DepositUseCase;
import com.br.walletcore.usecase.GetBalanceUseCase;
import com.br.walletcore.usecase.GetHistoricalBalance;
import com.br.walletcore.usecase.GetWalletUseCase;
import com.br.walletcore.usecase.TransferUseCase;
import com.br.walletcore.usecase.WithdrawUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletFacade {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetHistoricalBalance getHistoricalBalance;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetWalletUseCase getWalletUseCase;

    public Wallet createWallet(String userId, String currency) {
        return createWalletUseCase.execute(userId, currency);
    }

    public Money getBalance(String userId) {
        return getBalanceUseCase.execute(userId);
    }

    public Money getHistoricalBalance(String userId, LocalDate date) {
        return getHistoricalBalance.execute(userId, date);
    }

    public WalletTransaction deposit(String userId, Money amount) {
        return depositUseCase.execute(userId, amount);
    }

    public WalletTransaction withdraw(String userId, Money amount) {
        return withdrawUseCase.execute(userId, amount);
    }

    public List<WalletTransaction> transfer(String fromUserId, String toUserId, Money amount) {
        return transferUseCase.execute(fromUserId, toUserId, amount);
    }

    public Wallet getWallet(String userId) {
        return getWalletUseCase.execute(userId);
    }

}
