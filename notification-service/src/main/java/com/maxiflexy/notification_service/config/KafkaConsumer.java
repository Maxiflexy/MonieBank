package com.maxiflexy.notification_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.maxiflexy.notification_service.dto.NotificationDto;
import com.maxiflexy.common.dto.NotificationDto;
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
    public void consumeTransactionNotification(@Payload String payload) {
        logger.info("Received transaction notification raw payload: {}", payload);

        try {
            NotificationDto notification = parsePayload(payload);
            logger.info("Successfully deserialized transaction notification: {}", notification);
            notificationService.processNotification(notification);
        } catch (Exception e) {
            logger.error("Error processing transaction notification: {}", e.getMessage(), e);
            logger.error("Raw payload that failed: {}", payload);
        }
    }

    @KafkaListener(
            topics = "email-notifications",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeEmailNotification(@Payload String payload) {
        logger.info("Received email notification raw payload: {}", payload);

        try {
            NotificationDto notification = parsePayload(payload);
            logger.info("Successfully deserialized email notification: {}", notification);

            // Handle EMAIL_VERIFICATION notifications differently
            if ("EMAIL_VERIFICATION".equals(notification.getNotificationType())) {
                notificationService.sendDirectEmail(notification);
            } else {
                notificationService.processNotification(notification);
            }
        } catch (Exception e) {
            logger.error("Error processing email notification: {}", e.getMessage(), e);
            logger.error("Raw payload that failed: {}", payload);
        }
    }

    /**
     * Parses a payload string that might be a JSON string or a quoted JSON string
     */
    private NotificationDto parsePayload(String payload) throws Exception {
        // If the payload is a quoted JSON string (starts and ends with quotes)
        if (payload.startsWith("\"") && payload.endsWith("\"")) {
            // The payload is a quoted JSON string
            // First, parse it as a JSON string to get the inner content
            String innerJson = objectMapper.readValue(payload, String.class);
            // Then parse the inner content as a NotificationDto
            return objectMapper.readValue(innerJson, NotificationDto.class);
        } else {
            // Regular JSON object
            return objectMapper.readValue(payload, NotificationDto.class);
        }
    }
}