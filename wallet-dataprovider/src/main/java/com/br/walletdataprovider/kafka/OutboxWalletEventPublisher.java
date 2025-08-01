package com.br.walletdataprovider.kafka;

import com.br.walletcore.port.events.OutboxEventPublisher;
import com.br.walletdataprovider.mongodb.document.OutboxEventDocument;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.br.walletdataprovider.utils.EventPayloadAccessor.cleanPayloadForSerialization;
import static com.br.walletdataprovider.utils.EventPayloadAccessor.extractStringValue;

@Slf4j
@RequiredArgsConstructor
public class OutboxWalletEventPublisher implements OutboxEventPublisher {

    private final OutboxEventMongoRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${wallet.audit.enabled}")
    private boolean auditEnabled;

    @Value("${wallet.outbox.max-retries}")
    private int maxRetries;

    @Override
    public void publishOutboxEvent(String eventType, Object payload) {
        if (!auditEnabled) {
            log.debug("Audit disabled, skipping event publication: {}", eventType);
            return;
        }

        try {
            Object processedPayload = cleanPayloadForSerialization(payload, List.of("timestamp"));
            String eventData = objectMapper.writeValueAsString(processedPayload);
            String correlationId = extractStringValue(processedPayload, "correlationId")
                    .orElse(UUID.randomUUID().toString());
            String aggregateId = extractStringValue(processedPayload, "walletId").orElse("unknown");

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
}
