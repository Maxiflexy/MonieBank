package com.maxiflexy.notification_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import org.apache.kafka.clients.consumer.Consumer;

@Component("customErrorHandler")
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaErrorHandler.class);

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        logger.error("Error in Kafka message consumption: {}", exception.getMessage());
        logger.error("Message payload: {}", message.getPayload());
        logger.error("Message headers: {}", message.getHeaders());
        logger.error("Exception stacktrace:", exception);

        // If needed, you can implement retry logic or specific error handling here
        // For now, let's just log the error and continue

        return null; // Return null to indicate the message has been "handled"
    }
}