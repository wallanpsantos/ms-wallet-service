package com.br.walletdataprovider.kafka;

import com.br.walletdataprovider.mongodb.document.OutboxEventDocument;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "wallet.outbox.scheduler.enabled", havingValue = "true")
public class OutboxEventProcessor {

    private final OutboxEventMongoRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wallet.outbox.batch-size}")
    private int batchSize;

    @Value("${wallet.outbox.max-retries}")
    private int maxRetries;

    @Value("${wallet.kafka.topics.wallet-events.name}")
    private String walletEventsTopicName;

    @Value("${wallet.audit.enabled}")
    private boolean auditEnabled;

    @Scheduled(fixedDelayString = "${wallet.outbox.scheduler.fixed-delay}",
            initialDelayString = "${wallet.outbox.scheduler.initial-delay}")
    @Transactional
    public void processOutboxEvents() {
        if (!auditEnabled) {
            log.trace("Audit disabled, skipping outbox processing");
            return;
        }

        log.debug("Processing outbox events...");

        List<OutboxEventDocument> unprocessedEvents = outboxRepository
                .findUnprocessedEventsOlderThan(LocalDateTime.now().minusSeconds(1))
                .stream()
                .limit(batchSize)
                .toList();

        if (unprocessedEvents.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", unprocessedEvents.size());

        for (OutboxEventDocument event : unprocessedEvents) {
            try {
                publishEventToKafka(event);
                markAsProcessed(event);
                log.debug("Successfully processed outbox event: {}", event.getId());

            } catch (Exception e) {
                handleProcessingError(event, e);
            }
        }
    }

    private void publishEventToKafka(OutboxEventDocument event) throws Exception {
        Object eventData = objectMapper.readValue(event.getEventData(), Map.class);

        Map<String, Object> kafkaEvent = Map.of(
                "eventId", event.getId(),
                "eventType", event.getEventType(),
                "aggregateId", event.getAggregateId(),
                "payload", eventData,
                "createdAt", event.getCreatedAt(),
                "correlationId", event.getCorrelationId()
        );

        kafkaTemplate.send(walletEventsTopicName, event.getAggregateId(), kafkaEvent).get();
    }

    private void markAsProcessed(OutboxEventDocument event) {
        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }

    private void handleProcessingError(OutboxEventDocument event, Exception e) {
        log.error("Failed to process outbox event: {}", event.getId(), e);

        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(e.getMessage());

        if (event.getRetryCount() >= maxRetries) {
            log.error("Max retries ({}) reached for outbox event: {}. Moving to DLQ.", maxRetries, event.getId());
            // In production, implement Dead Letter Queue
        }

        outboxRepository.save(event);
    }
}