package com.br.walletdataprovider.mongodb.repository.impl;

import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.port.repositories.WalletRepository;
import com.br.walletdataprovider.mongodb.mapper.WalletMapper;
import com.br.walletdataprovider.mongodb.mapper.WalletTransactionMapper;
import com.br.walletdataprovider.mongodb.repository.WalletMongoRepository;
import com.br.walletdataprovider.mongodb.repository.WalletTransactionMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletMongoRepository walletMongoRepository;
    private final WalletTransactionMongoRepository transactionMongoRepository;
    private final WalletMapper walletMapper;
    private final WalletTransactionMapper transactionMapper;

    @Override
    public Wallet save(Wallet wallet) {
        var document = walletMapper.toDocument(wallet);
        var saved = walletMongoRepository.save(document);
        return walletMapper.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findByUserId(String userId) {
        return walletMongoRepository.findByUserId(userId)
                .map(walletMapper::toDomain);
    }

    @Override
    public List<WalletTransaction> findTransactionsByWalletIdUntilTimestamp(String walletId, LocalDateTime timestamp) {
        return transactionMongoRepository
                .findByWalletIdAndTimestampLessThanEqualOrderByTimestampAsc(walletId, timestamp)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public void saveTransaction(WalletTransaction transaction) {
        var document = transactionMapper.toDocument(transaction);
        transactionMongoRepository.save(document);
    }
}