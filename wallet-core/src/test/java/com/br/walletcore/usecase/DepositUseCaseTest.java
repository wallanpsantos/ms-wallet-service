package com.br.walletcore.usecase;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.enums.TransactionType;
import com.br.walletcore.port.events.OutboxEventPublisher;
import com.br.walletcore.port.events.WalletEventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletEventPublisher walletEventPublisher;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private DepositUseCase depositUseCase;

    @Test
    @DisplayName("Should deposit successfully")
    void shouldDepositSuccessfully() {
        // Given
        String userId = "user-123";
        Money depositAmount = Money.of(new BigDecimal("100.00"), "BRL");
        Wallet wallet = Wallet.builder()
                .id("wallet-123")
                .userId(userId)
                .balance(Money.of(BigDecimal.ZERO, "BRL"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WalletTransaction result = depositUseCase.execute(userId, depositAmount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(depositAmount);
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getBalanceAfter().getAmount()).isEqualTo(new BigDecimal("100.00"));

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).saveTransaction(any(WalletTransaction.class));
        verify(walletEventPublisher).publishWalletEvent(anyString(), anyMap());
        verify(outboxEventPublisher).publishOutboxEvent(anyString(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowExceptionWhenWalletNotFound() {
        // Given
        String userId = "user-123";
        Money depositAmount = Money.of(new BigDecimal("100.00"), "BRL");

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> depositUseCase.execute(userId, depositAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wallet not found for user: " + userId);

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(walletEventPublisher, outboxEventPublisher);
    }
}
