package com.maxiflexy.transaction_service.config;

import com.maxiflexy.transaction_service.dto.NotificationDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic transactionNotificationTopic() {
        return TopicBuilder.name("transaction-notifications")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, NotificationDto> kafkaTemplate(ProducerFactory<String, NotificationDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
