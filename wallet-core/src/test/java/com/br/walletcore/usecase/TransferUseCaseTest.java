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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletEventPublisher walletEventPublisher;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private TransferUseCase transferUseCase;

    @Test
    @DisplayName("Should transfer successfully")
    void shouldTransferSuccessfully() {
        // Given
        String fromUserId = "user-1";
        String toUserId = "user-2";
        Money amount = Money.of(new BigDecimal("50.00"), "BRL");

        Wallet sourceWallet = Wallet.builder()
                .id("wallet-1")
                .userId(fromUserId)
                .balance(Money.of(new BigDecimal("100.00"), "BRL"))
                .build();

        Wallet targetWallet = Wallet.builder()
                .id("wallet-2")
                .userId(toUserId)
                .balance(Money.of(new BigDecimal("10.00"), "BRL"))
                .build();

        when(walletRepository.findByUserId(fromUserId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.of(targetWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<WalletTransaction> result = transferUseCase.execute(fromUserId, toUserId, amount);

        // Then
        assertThat(result).hasSize(2);
        
        WalletTransaction withdraw = result.stream().filter(t -> t.getType() == TransactionType.TRANSFER_OUT).findFirst().orElseThrow();
        assertThat(withdraw.getAmount()).isEqualTo(amount);
        assertThat(withdraw.getBalanceAfter().getAmount()).isEqualTo(new BigDecimal("50.00"));

        WalletTransaction deposit = result.stream().filter(t -> t.getType() == TransactionType.TRANSFER_IN).findFirst().orElseThrow();
        assertThat(deposit.getAmount()).isEqualTo(amount);
        assertThat(deposit.getBalanceAfter().getAmount()).isEqualTo(new BigDecimal("60.00"));

        verify(walletRepository, times(2)).findByUserId(anyString());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(walletRepository, times(2)).saveTransaction(any(WalletTransaction.class));
        verify(walletEventPublisher).publishWalletEvent(anyString(), anyMap());
        verify(outboxEventPublisher).publishOutboxEvent(anyString(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when transferring to same user")
    void shouldThrowExceptionWhenTransferringToSameUser() {
        // Given
        String userId = "user-1";
        Money amount = Money.of(new BigDecimal("50.00"), "BRL");

        // When & Then
        assertThatThrownBy(() -> transferUseCase.execute(userId, userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer to same user");

        verifyNoInteractions(walletRepository, walletEventPublisher, outboxEventPublisher);
    }

    @Test
    @DisplayName("Should throw exception when source wallet not found")
    void shouldThrowExceptionWhenSourceWalletNotFound() {
        // Given
        String fromUserId = "user-1";
        String toUserId = "user-2";
        Money amount = Money.of(new BigDecimal("50.00"), "BRL");

        when(walletRepository.findByUserId(fromUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transferUseCase.execute(fromUserId, toUserId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source wallet not found");

        verify(walletRepository).findByUserId(fromUserId);
        verifyNoMoreInteractions(walletRepository);
    }

    @Test
    @DisplayName("Should throw exception when currency mismatch")
    void shouldThrowExceptionWhenCurrencyMismatch() {
        // Given
        String fromUserId = "user-1";
        String toUserId = "user-2";
        Money amount = Money.of(new BigDecimal("50.00"), "BRL");

        Wallet sourceWallet = Wallet.builder()
                .id("wallet-1")
                .userId(fromUserId)
                .balance(Money.of(new BigDecimal("100.00"), "BRL"))
                .build();

        Wallet targetWallet = Wallet.builder()
                .id("wallet-2")
                .userId(toUserId)
                .balance(Money.of(new BigDecimal("10.00"), "USD"))
                .build();

        when(walletRepository.findByUserId(fromUserId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.of(targetWallet));

        // When & Then
        assertThatThrownBy(() -> transferUseCase.execute(fromUserId, toUserId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currency mismatch between wallets");

        verify(walletRepository, times(2)).findByUserId(anyString());
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
