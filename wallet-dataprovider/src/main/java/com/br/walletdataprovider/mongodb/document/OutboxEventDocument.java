package com.br.walletdataprovider.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "outbox_events")
public class OutboxEventDocument {
    @Id
    private String id;

    @Indexed
    private String aggregateId; // walletId

    private String eventType;
    private String eventData; // JSON serializado

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private boolean processed;

    private LocalDateTime processedAt;
    private int retryCount;
    private String errorMessage;

    @Indexed
    private String correlationId;
}
