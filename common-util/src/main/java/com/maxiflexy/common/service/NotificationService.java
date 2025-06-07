package com.maxiflexy.common.service;

import com.maxiflexy.common.dto.EmailNotificationDto;
import com.maxiflexy.common.dto.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String TRANSACTION_NOTIFICATIONS_TOPIC = "transaction-notifications";
    private static final String EMAIL_NOTIFICATIONS_TOPIC = "email-notifications";

    @Autowired(required = false)
    private KafkaTemplate<String, NotificationDto> transactionKafkaTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, EmailNotificationDto> emailKafkaTemplate;

    /**
     * Send transaction notification to Kafka
     */
    public void sendTransactionNotification(NotificationDto notification) {
        if (transactionKafkaTemplate == null) {
            logger.warn("Transaction Kafka template not available. Notification not sent: {}", notification);
            return;
        }

        logger.info("Sending transaction notification to Kafka: {}", notification);

        Message<NotificationDto> message = MessageBuilder
                .withPayload(notification)
                .setHeader(KafkaHeaders.TOPIC, TRANSACTION_NOTIFICATIONS_TOPIC)
                .setHeader("spring.json.type.mapping", "notificationdto:com.maxiflexy.common.dto.NotificationDto")
                .build();

        transactionKafkaTemplate.send(message);
    }

    /**
     * Send email notification to Kafka
     */
    public void sendEmailNotification(EmailNotificationDto notification) {
        if (emailKafkaTemplate == null) {
            logger.warn("Email Kafka template not available. Notification not sent: {}", notification);
            return;
        }

        logger.info("Sending email notification to Kafka: {}", notification);

        Message<EmailNotificationDto> message = MessageBuilder
                .withPayload(notification)
                .setHeader(KafkaHeaders.TOPIC, EMAIL_NOTIFICATIONS_TOPIC)
                .setHeader("spring.json.type.mapping", "emailnotification:com.maxiflexy.common.dto.EmailNotificationDto")
                .build();

        emailKafkaTemplate.send(message);
    }
}