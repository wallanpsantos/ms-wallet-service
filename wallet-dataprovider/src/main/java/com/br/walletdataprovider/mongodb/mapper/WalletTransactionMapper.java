package com.br.walletdataprovider.mongodb.mapper;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.enums.TransactionType;
import com.br.walletdataprovider.mongodb.document.WalletTransactionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(target = "type", expression = "java(transaction.getType().name())")
    @Mapping(target = "amount", expression = "java(transaction.getAmount().getAmount())")
    @Mapping(target = "currency", expression = "java(transaction.getAmount().getCurrency())")
    @Mapping(target = "balanceAfter", expression = "java(transaction.getBalanceAfter().getAmount())")
    @Mapping(target = "balanceAfterCurrency", expression = "java(transaction.getBalanceAfter().getCurrency())")
    WalletTransactionDocument toDocument(WalletTransaction transaction);

    @Mapping(target = "type", expression = "java(mapTransactionType(document.getType()))")
    @Mapping(target = "amount", expression = "java(mapAmount(document))")
    @Mapping(target = "balanceAfter", expression = "java(mapBalanceAfter(document))")
    WalletTransaction toDomain(WalletTransactionDocument document);

    default TransactionType mapTransactionType(String type) {
        return TransactionType.valueOf(type);
    }

    default Money mapAmount(WalletTransactionDocument document) {
        return Money.of(document.getAmount(), document.getCurrency());
    }

    default Money mapBalanceAfter(WalletTransactionDocument document) {
        return Money.of(document.getBalanceAfter(), document.getBalanceAfterCurrency());
    }
}
