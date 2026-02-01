package com.br.walletdataprovider.mongodb.repository.impl;

import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletdataprovider.mongodb.document.WalletDocument;
import com.br.walletdataprovider.mongodb.document.WalletTransactionDocument;
import com.br.walletdataprovider.mongodb.mapper.WalletMapper;
import com.br.walletdataprovider.mongodb.mapper.WalletTransactionMapper;
import com.br.walletdataprovider.mongodb.repository.WalletMongoRepository;
import com.br.walletdataprovider.mongodb.repository.WalletTransactionMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletRepositoryImplTest {

    @Mock
    private WalletMongoRepository walletMongoRepository;

    @Mock
    private WalletTransactionMongoRepository transactionMongoRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private WalletTransactionMapper transactionMapper;

    @InjectMocks
    private WalletRepositoryImpl walletRepositoryImpl;

    @Test
    @DisplayName("Should save wallet successfully")
    void shouldSaveWalletSuccessfully() {
        // Given
        Wallet wallet = new Wallet();
        WalletDocument document = new WalletDocument();

        when(walletMapper.toDocument(wallet)).thenReturn(document);
        when(walletMongoRepository.save(document)).thenReturn(document);
        when(walletMapper.toDomain(document)).thenReturn(wallet);

        // When
        Wallet result = walletRepositoryImpl.save(wallet);

        // Then
        assertThat(result).isEqualTo(wallet);
        verify(walletMapper).toDocument(wallet);
        verify(walletMongoRepository).save(document);
        verify(walletMapper).toDomain(document);
    }

    @Test
    @DisplayName("Should find wallet by user id")
    void shouldFindWalletByUserId() {
        // Given
        String userId = "user-123";
        WalletDocument document = new WalletDocument();
        Wallet wallet = new Wallet();

        when(walletMongoRepository.findByUserId(userId)).thenReturn(Optional.of(document));
        when(walletMapper.toDomain(document)).thenReturn(wallet);

        // When
        Optional<Wallet> result = walletRepositoryImpl.findByUserId(userId);

        // Then
        assertThat(result).isPresent().contains(wallet);
        verify(walletMongoRepository).findByUserId(userId);
        verify(walletMapper).toDomain(document);
    }

    @Test
    @DisplayName("Should save transaction successfully")
    void shouldSaveTransactionSuccessfully() {
        // Given
        WalletTransaction transaction = new WalletTransaction();
        WalletTransactionDocument document = new WalletTransactionDocument();

        when(transactionMapper.toDocument(transaction)).thenReturn(document);

        // When
        walletRepositoryImpl.saveTransaction(transaction);

        // Then
        verify(transactionMapper).toDocument(transaction);
        verify(transactionMongoRepository).save(document);
    }
}
