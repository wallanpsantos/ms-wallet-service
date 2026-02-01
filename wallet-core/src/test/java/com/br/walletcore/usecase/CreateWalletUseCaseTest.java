package com.br.walletcore.usecase;

import com.br.walletcore.domain.Wallet;
import com.br.walletcore.port.events.OutboxEventPublisher;
import com.br.walletcore.port.events.WalletEventPublisher;
import com.br.walletcore.port.repositories.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletEventPublisher walletEventPublisher;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @InjectMocks
    private CreateWalletUseCase createWalletUseCase;

    @Test
    @DisplayName("Should create wallet successfully when user does not have one")
    void shouldCreateWalletSuccessfully() {
        // Given
        String userId = "user-123";
        String currency = "BRL";
        
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            wallet.setId("wallet-id-123");
            return wallet;
        });

        // When
        Wallet result = createWalletUseCase.execute(userId, currency);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getBalance().getAmount()).isZero();
        assertThat(result.getBalance().getCurrency()).isEqualTo(currency);
        
        verify(walletRepository).findByUserId(userId);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletEventPublisher).publishWalletEvent(anyString(), anyMap());
        verify(outboxEventPublisher).publishOutboxEvent(anyString(), anyMap());
    }

    @Test
    @DisplayName("Should throw exception when user already has a wallet")
    void shouldThrowExceptionWhenUserAlreadyHasWallet() {
        // Given
        String userId = "user-123";
        String currency = "BRL";
        
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(new Wallet()));

        // When & Then
        assertThatThrownBy(() -> createWalletUseCase.execute(userId, currency))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User already has a wallet");

        verify(walletRepository).findByUserId(userId);
        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(walletEventPublisher, outboxEventPublisher);
    }
}
