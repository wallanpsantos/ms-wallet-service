package com.br.walletconfig.kafka;

import com.br.walletcore.port.events.EventPublisher;
import com.br.walletdataprovider.kafka.KafkaEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventPublisherConfig {

    @Bean("kafkaEventPublisher")
    @Primary
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    // Other implementations of eventpublisher in the future
    // @Bean("asyncEventPublisher")
    // public EventPublisher asyncEventPublisher() {
    //     return new AsyncEventPublisher();
    // }
}
