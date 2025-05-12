package com.maxiflexy.auth_service.service;

import com.maxiflexy.auth_service.dto.EmailNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(NotificationSender.class);
    private static final String TOPIC = "email-notifications";

    @Autowired
    private KafkaTemplate<String, EmailNotificationDto> kafkaTemplate;

    public void sendEmailNotification(EmailNotificationDto notification) {
        logger.info("Sending email notification to Kafka: {}", notification);
        kafkaTemplate.send(TOPIC, notification);
    }
}