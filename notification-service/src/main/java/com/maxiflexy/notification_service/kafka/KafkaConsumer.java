package com.maxiflexy.notification_service.kafka;

import com.maxiflexy.notification_service.dto.NotificationDto;
import com.maxiflexy.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = "transaction-notifications", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotification(NotificationDto notification) {
        logger.info("Received notification: {}", notification);
        notificationService.processNotification(notification);
    }
}
