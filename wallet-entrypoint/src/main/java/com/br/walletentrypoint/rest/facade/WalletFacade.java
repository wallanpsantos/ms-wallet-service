package com.br.walletentrypoint.rest.facade;

import com.br.walletcore.domain.Money;
import com.br.walletcore.usecase.CreateWalletUseCase;
import com.br.walletcore.usecase.DepositUseCase;
import com.br.walletcore.usecase.GetBalanceUseCase;
import com.br.walletcore.usecase.GetHistoricalBalance;
import com.br.walletcore.usecase.GetWalletUseCase;
import com.br.walletcore.usecase.TransferUseCase;
import com.br.walletcore.usecase.WithdrawUseCase;
import com.br.walletentrypoint.rest.mapper.WalletResponseMapper;
import com.br.walletentrypoint.rest.response.BalanceResponse;
import com.br.walletentrypoint.rest.response.TransactionResponse;
import com.br.walletentrypoint.rest.response.WalletResponse;
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

    private final WalletResponseMapper responseMapper;

    public WalletResponse createWallet(final String userId, final String currency) {
        return responseMapper.toWalletResponse(createWalletUseCase.execute(userId, currency));
    }

    public BalanceResponse getBalance(final String userId) {
        Money money = getBalanceUseCase.execute(userId);
        return responseMapper.toBalanceResponse(userId, money);
    }

    public BalanceResponse getHistoricalBalance(final String userId, final LocalDate date) {
        Money money = getHistoricalBalance.execute(userId, date);
        return responseMapper.toBalanceResponse(userId, money);
    }

    public TransactionResponse deposit(final String userId, final Money amount) {
        return responseMapper.toTransactionResponse(depositUseCase.execute(userId, amount));
    }

    public TransactionResponse withdraw(final String userId, final Money amount) {
        return responseMapper.toTransactionResponse(withdrawUseCase.execute(userId, amount));
    }

    public List<TransactionResponse> transfer(final String fromUserId, final String toUserId, final Money amount) {
        return responseMapper.toTransactionResponseList(transferUseCase.execute(fromUserId, toUserId, amount));
    }

    public WalletResponse getWallet(final String userId) {
        return responseMapper.toWalletResponse(getWalletUseCase.execute(userId));
    }

}
