package com.maxiflexy.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxiflexy.notification_service.dto.NotificationDto;
import com.maxiflexy.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(
            topics = "transaction-notifications",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransactionNotification(@Payload Object payload) {
        logger.info("Received transaction notification: {}", payload);

        try {
            // Convert payload to NotificationDto
            NotificationDto notification = convertToNotificationDto(payload);
            notificationService.processNotification(notification);
        } catch (Exception e) {
            logger.error("Error processing transaction notification: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "email-notifications",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeEmailNotification(@Payload Object payload) {
        logger.info("Received email notification: {}", payload);

        try {
            // Convert payload to NotificationDto
            NotificationDto notification = convertToNotificationDto(payload);

            // Handle EMAIL_VERIFICATION notifications differently
            if ("EMAIL_VERIFICATION".equals(notification.getNotificationType())) {
                notificationService.sendDirectEmail(notification);
            } else {
                notificationService.processNotification(notification);
            }
        } catch (Exception e) {
            logger.error("Error processing email notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Converts a payload object to NotificationDto
     */
    private NotificationDto convertToNotificationDto(Object payload) {
        try {
            if (payload instanceof NotificationDto) {
                return (NotificationDto) payload;
            } else if (payload instanceof String) {
                return objectMapper.readValue((String) payload, NotificationDto.class);
            } else {
                return objectMapper.convertValue(payload, NotificationDto.class);
            }
        } catch (Exception e) {
            logger.error("Error converting payload to NotificationDto: {}", e.getMessage());
            throw new RuntimeException("Failed to convert payload", e);
        }
    }
}