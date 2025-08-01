package com.br.walletdataprovider.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Processes Events for Audit/Notifications
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WalletEventConsumer {

    @KafkaListener(
            topics = "wallet-events",
            groupId = "wallet-audit-consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleWalletEvent(@Payload Map<String, Object> event,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        try {
            String eventType = (String) event.get("eventType");
            String eventId = (String) event.get("eventId");
            String aggregateId = (String) event.get("aggregateId");
            log.info("Processing wallet event: {} for aggregate: {} from topic: {}, partition: {}, offset: {}",
                    eventType, aggregateId, topic, partition, offset);
            // Process different types of events
            switch (eventType) {
                case "WALLET_CREATED" -> handleWalletCreated(event);
                case "FUNDS_DEPOSITED" -> handleFundsDeposited(event);
                case "FUNDS_WITHDRAWN" -> handleFundsWithdrawn(event);
                case "FUNDS_TRANSFERRED" -> handleFundsTransferred(event);
                default -> log.warn("Unknown event type: {}", eventType);
            }
            // Ensures that messages are only confirmed after processing
            acknowledgment.acknowledge();
            log.info("Successfully processed event: {}", eventId);
        } catch (Exception e) {
            log.error("Error processing wallet event: {}", event, e);
            // In production, implement retry logic or send to DLQ
            acknowledgment.acknowledge(); // To avoid infinite reprocessing
        }
    }

    private void handleWalletCreated(Map<String, Object> event) {
        // Implement specific logic for wallet creation
        // E.g.: send welcome email, update cache, etc.
        log.info("Wallet created event processed: {}", event);
    }

    private void handleFundsDeposited(Map<String, Object> event) {
        // Implement specific logic for deposit
        // E.g.: notifications, fraud analysis, etc.
        log.info("Funds deposited event processed: {}", event);
    }

    private void handleFundsWithdrawn(Map<String, Object> event) {
        // Implement specific logic for withdrawal
        // E.g.: additional validations, compliance, etc.
        log.info("Funds withdrawn event processed: {}", event);
    }

    private void handleFundsTransferred(Map<String, Object> event) {
        // Implement specific logic for transfer
        // E.g.: pattern analysis, reports, etc.
        log.info("Funds transferred event processed: {}", event);
    }
}