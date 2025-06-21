package com.maxiflexy.auth_service.service;

import com.maxiflexy.auth_service.dto.EmailNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);
    private static final String TOPIC = "email-notifications";

    @Autowired
    private KafkaTemplate<String, EmailNotificationDto> kafkaTemplate;

    public void sendEmailNotification(EmailNotificationDto notification) {
        logger.info("Sending email notification to Kafka: {}", notification);

        // Add type header for proper deserialization on consumer side
        Message<EmailNotificationDto> message = MessageBuilder
                .withPayload(notification)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader("spring.json.type.mapping", "emailnotification:com.maxiflexy.auth_service.dto.EmailNotificationDto")
                .build();

        kafkaTemplate.send(message);
    }
}