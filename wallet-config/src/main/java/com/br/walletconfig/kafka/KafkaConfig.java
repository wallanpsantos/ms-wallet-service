package com.br.walletconfig.kafka;

import com.fasterxml.jackson.databind.JsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tools.jackson.databind.ser.jdk.StringSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Configuração do Kafka para Spring Boot 4.x</p>
 * <p>No Spring Boot 4.x, o KafkaTemplate<String, Object> precisa ser criado explicitamente</p>
 * <p>Demais configurações (producer, consumer, listener) estão todas no application.yml</p>
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

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

    /**
     * Configuração do ProducerFactory para KafkaTemplate<String, Object>
     * Necessário no Spring Boot 4.x para registrar o bean corretamente
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configurações adicionais do application.yml serão mescladas automaticamente
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Garante que todas as réplicas confirmem
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Tente 3 vezes
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); //Evita duplicatas

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Bean do KafkaTemplate<String, Object>
     * Necessário no Spring Boot 4.x - a autoconfiguração não cria mais este bean genérico
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}