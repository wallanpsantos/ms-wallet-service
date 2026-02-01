package com.br.walletdataprovider.kafka;

import com.br.walletdataprovider.mongodb.document.OutboxEventDocument;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventMongoRepository outboxRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxEventProcessor, "batchSize", 10);
        ReflectionTestUtils.setField(outboxEventProcessor, "maxRetries", 3);
        ReflectionTestUtils.setField(outboxEventProcessor, "walletOutboxTopicName", "wallet-outbox");
        ReflectionTestUtils.setField(outboxEventProcessor, "auditEnabled", true);
    }

    @Test
    @DisplayName("Should process outbox events successfully")
    void shouldProcessOutboxEventsSuccessfully() {
        // Given
        OutboxEventDocument event = OutboxEventDocument.builder()
                .id("event-1")
                .eventData("{\"key\":\"value\"}")
                .eventType("TEST_EVENT")
                .aggregateId("agg-1")
                .createdAt(LocalDateTime.now())
                .correlationId("corr-1")
                .build();

        when(outboxRepository.findUnprocessedEventsOlderThan(any())).thenReturn(List.of(event));
        when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(Map.of("key", "value"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        // When
        outboxEventProcessor.processOutboxEvents();

        // Then
        verify(outboxRepository).findUnprocessedEventsOlderThan(any());
        verify(kafkaTemplate).send(eq("wallet-outbox"), eq("agg-1"), anyMap());
        verify(outboxRepository).save(argThat(OutboxEventDocument::isProcessed));
    }

//    @Test
//    @DisplayName("Should handle error and increment retry count")
//    void shouldHandleErrorAndIncrementRetryCount() throws Exception {
//        // Given
//        OutboxEventDocument event = OutboxEventDocument.builder()
//                .id("event-1")
//                .eventData("{\"key\":\"value\"}")
//                .retryCount(0)
//                .build();
//
//        when(outboxRepository.findUnprocessedEventsOlderThan(any())).thenReturn(List.of(event));
//        when(objectMapper.readValue(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Mapping error"));
//
//        // When
//        outboxEventProcessor.processOutboxEvents();
//
//        // Then
//        verify(outboxRepository).save(argThat(e -> e.getRetryCount() == 1 && e.getErrorMessage().equals("Mapping error")));
//    }
}
