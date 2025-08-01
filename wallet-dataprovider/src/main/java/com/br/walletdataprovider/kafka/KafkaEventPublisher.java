package com.br.walletdataprovider.kafka;

import com.br.walletcore.port.events.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Qualifier("kafkaEventPublisher")
public class KafkaEventPublisher implements EventPublisher {

    @Value("${wallet.kafka.topics.wallet-events.name}")
    private String walletEventsTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishWalletEvent(String eventType, Object payload) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "payload", payload,
                    "timestamp", LocalDateTime.now().toString(),
                    "eventId", UUID.randomUUID().toString()
            );

            kafkaTemplate.send(walletEventsTopic, eventType, event);
            log.info("Published event: {} to topic: {}", eventType, walletEventsTopic);

        } catch (Exception e) {
            log.error("Failed to publish event: {}", eventType, e);
            // In a real environment, it can implement retry or dead Letter Queue
        }
    }
}
