package com.br.walletconfig.kafka;

import com.br.walletcore.port.events.OutboxEventPublisher;
import com.br.walletcore.port.events.WalletEventPublisher;
import com.br.walletdataprovider.kafka.KafkaWalletEventPublisher;
import com.br.walletdataprovider.kafka.OutboxWalletEventPublisher;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventPublisherConfig {

    @Bean("kafkaEventPublisher")
    public WalletEventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaWalletEventPublisher(kafkaTemplate);
    }

    @Bean("outboxEventPublisher")
    public OutboxEventPublisher outboxEventPublisher(OutboxEventMongoRepository outboxRepository, ObjectMapper objectMapper) {
        return new OutboxWalletEventPublisher(outboxRepository, objectMapper);
    }

}
