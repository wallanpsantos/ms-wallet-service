package com.br.walletconfig.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuração simplificada do Kafka usando apenas @Value.
 * As demais configurações (producer, consumer, listener) estão todas no application.yml
 * e são aplicadas automaticamente pela autoconfiguração do Spring Boot.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${wallet.kafka.topics.wallet-events.name}")
    private String walletEventsTopicName;

    @Value("${wallet.kafka.topics.wallet-events.partitions}")
    private int walletEventsPartitions;

    @Value("${wallet.kafka.topics.wallet-events.replicas}")
    private int walletEventsReplicas;

    @Value("${wallet.kafka.topics.wallet-outbox.name}")
    private String walletOutboxTopicName;

    @Value("${wallet.kafka.topics.wallet-outbox.partitions}")
    private int walletOutboxPartitions;

    @Value("${wallet.kafka.topics.wallet-outbox.replicas}")
    private int walletOutboxReplicas;

    /**
     * Cria o tópico para eventos de wallet se não existir.
     * Configurações do producer/consumer vêm do application.yml
     */
    @Bean
    public NewTopic walletEventsTopic() {
        return TopicBuilder.name(walletEventsTopicName)
                .partitions(walletEventsPartitions)
                .replicas(walletEventsReplicas)
                .build();
    }

    /**
     * Cria o tópico para eventos de outbox se não existir.
     * Configurações do producer/consumer vêm do application.yml
     */
    @Bean
    public NewTopic walletOutboxTopic() {
        return TopicBuilder.name(walletOutboxTopicName)
                .partitions(walletOutboxPartitions)
                .replicas(walletOutboxReplicas)
                .build();
    }
}