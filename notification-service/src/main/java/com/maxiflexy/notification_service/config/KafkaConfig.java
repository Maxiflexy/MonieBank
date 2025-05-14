package com.maxiflexy.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxiflexy.notification_service.dto.NotificationDto;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Autowired
    private ObjectMapper objectMapper;

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public RecordMessageConverter recordMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // Get default properties
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        // Configure the JsonDeserializer with super lenient settings
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.Object");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Add ErrorHandlingDeserializer
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class.getName());
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                StringDeserializer.class.getName());

        // Create the consumer factory
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler());

        return factory;
    }

    @Bean
    public CommonErrorHandler errorHandler() {
        // Create an error handler with retry options
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                // Retry 3 times with 1 second intervals
                new FixedBackOff(1000L, 3));

        // Log errors but continue processing
        errorHandler.setCommitRecovered(true);

        return errorHandler;
    }
}