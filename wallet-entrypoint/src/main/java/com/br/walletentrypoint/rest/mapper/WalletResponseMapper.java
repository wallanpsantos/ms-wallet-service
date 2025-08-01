package com.br.walletentrypoint.rest.mapper;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletentrypoint.rest.response.BalanceResponse;
import com.br.walletentrypoint.rest.response.TransactionResponse;
import com.br.walletentrypoint.rest.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletResponseMapper {

    @Mapping(target = "balance", expression = "java(wallet.getBalance().getAmount())")
    @Mapping(target = "currency", expression = "java(wallet.getBalance().getCurrency())")
    WalletResponse toWalletResponse(Wallet wallet);

    @Mapping(target = "type", expression = "java(transaction.getType().name())")
    @Mapping(target = "amount", expression = "java(transaction.getAmount().getAmount())")
    @Mapping(target = "currency", expression = "java(transaction.getAmount().getCurrency())")
    @Mapping(target = "balanceAfter", expression = "java(transaction.getBalanceAfter().getAmount())")
    TransactionResponse toTransactionResponse(WalletTransaction transaction);

    List<TransactionResponse> toTransactionResponseList(List<WalletTransaction> transactions);

    @Mapping(target = "balance", expression = "java(balance.getAmount())")
    @Mapping(target = "currency", expression = "java(balance.getCurrency())")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    BalanceResponse toBalanceResponse(String userId, Money balance);

}