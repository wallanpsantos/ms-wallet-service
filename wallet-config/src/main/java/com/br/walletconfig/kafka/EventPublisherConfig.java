package com.br.walletconfig.kafka;

import com.br.walletcore.port.events.EventPublisher;
import com.br.walletdataprovider.kafka.KafkaEventPublisher;
import com.br.walletdataprovider.kafka.OutboxEventPublisher;
import com.br.walletdataprovider.mongodb.repository.OutboxEventMongoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventPublisherConfig {

    @Bean("kafkaEventPublisher")
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean("outboxEventPublisher")
    @Primary
    public EventPublisher outboxEventPublisher(OutboxEventMongoRepository outboxRepository, ObjectMapper objectMapper) {
        return new OutboxEventPublisher(outboxRepository, objectMapper);
    }

}
