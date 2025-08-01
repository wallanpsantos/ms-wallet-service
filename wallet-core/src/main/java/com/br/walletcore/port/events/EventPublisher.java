package com.br.walletcore.port.events;

public interface EventPublisher {
    void publishWalletEvent(String eventType, Object payload);
}