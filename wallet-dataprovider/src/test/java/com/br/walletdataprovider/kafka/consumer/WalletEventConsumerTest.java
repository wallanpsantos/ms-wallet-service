package com.br.walletdataprovider.kafka.consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventConsumerTest {

    @InjectMocks
    private WalletEventConsumer walletEventConsumer;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    @DisplayName("Should process WALLET_CREATED event successfully")
    void shouldProcessWalletCreatedEvent() {
        // Given
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "WALLET_CREATED");
        event.put("eventId", "evt-1");
        event.put("aggregateId", "agg-1");

        // When
        walletEventConsumer.handleWalletEvent(event, "wallet-events", 0, 1L, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should process FUNDS_DEPOSITED event successfully")
    void shouldProcessFundsDepositedEvent() {
        // Given
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FUNDS_DEPOSITED");
        event.put("eventId", "evt-2");
        event.put("aggregateId", "agg-2");

        // When
        walletEventConsumer.handleWalletEvent(event, "wallet-events", 0, 2L, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should handle unknown event type gracefully")
    void shouldHandleUnknownEventType() {
        // Given
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "UNKNOWN_TYPE");
        event.put("eventId", "evt-3");
        event.put("aggregateId", "agg-3");

        // When
        walletEventConsumer.handleWalletEvent(event, "wallet-events", 0, 3L, acknowledgment);

        // Then
        verify(acknowledgment).acknowledge();
    }
}
