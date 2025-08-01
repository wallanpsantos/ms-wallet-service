package com.br.walletcore.port.events;

public interface WalletEventPublisher {
    void publishWalletEvent(String eventType, Object payload);
}