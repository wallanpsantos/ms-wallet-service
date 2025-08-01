package com.br.walletdataprovider.kafka;

import com.br.walletcore.port.events.EventPublisher;
import com.br.walletdataprovider.mongodb.document.OutboxEventDocument;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxEventMongoRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${wallet.audit.enabled}")
    private boolean auditEnabled;

    @Value("${wallet.outbox.max-retries}")
    private int maxRetries;

    @Override
    public void publishWalletEvent(String eventType, Object payload) {
        if (!auditEnabled) {
            log.debug("Audit disabled, skipping event publication: {}", eventType);
            return;
        }

        try {
            String eventData = objectMapper.writeValueAsString(payload);
            String correlationId = extractCorrelationId(payload);
            String aggregateId = extractAggregateId(payload);

            var outboxEvent = OutboxEventDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .eventData(eventData)
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .retryCount(0)
                    .correlationId(correlationId)
                    .build();

            outboxRepository.save(outboxEvent);
            log.debug("Outbox event created: {} for aggregate: {}", eventType, aggregateId);

        } catch (Exception e) {
            log.error("Failed to create outbox event: {}", eventType, e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }

    private String extractCorrelationId(Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) payload;
            Object correlationId = map.get("correlationId");
            return correlationId != null ? correlationId.toString() : UUID.randomUUID().toString();
        }
        return UUID.randomUUID().toString();
    }

    private String extractAggregateId(Object payload) {
        if (payload instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) payload;
            Object walletId = map.get("walletId");
            return walletId != null ? walletId.toString() : "unknown";
        }
        return "unknown";
    }
}