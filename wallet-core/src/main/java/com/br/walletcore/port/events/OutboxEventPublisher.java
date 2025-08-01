package com.br.walletcore.port.events;

public interface OutboxEventPublisher {
    void publishOutboxEvent(String eventType, Object payload);
}