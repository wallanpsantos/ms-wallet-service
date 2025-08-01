package com.br.walletdataprovider.mongodb.mapper;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletdataprovider.mongodb.document.WalletDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "balance", expression = "java(mapBalance(wallet))")
    @Mapping(target = "currency", expression = "java(wallet.getBalance().getCurrency())")
    WalletDocument toDocument(Wallet wallet);

    @Mapping(target = "balance", expression = "java(mapMoney(document))")
    Wallet toDomain(WalletDocument document);

    default BigDecimal mapBalance(Wallet wallet) {
        return wallet.getBalance().getAmount();
    }

    default Money mapMoney(WalletDocument document) {
        return Money.of(document.getBalance(), document.getCurrency());
    }
}