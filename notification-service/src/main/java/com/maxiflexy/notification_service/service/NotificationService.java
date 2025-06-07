package com.maxiflexy.notification_service.service;

//import com.maxiflexy.notification_service.dto.NotificationDto;
import com.maxiflexy.common.dto.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private EmailService emailService;

    public void processNotification(NotificationDto notification) {
        logger.info("Processing transaction notification: {}", notification);

        String emailBody = buildEmailBody(notification);
        emailService.sendEmail(notification.getRecipientEmail(), notification.getSubject(), emailBody);
    }

    public void sendDirectEmail(NotificationDto notification) {
        logger.info("Sending direct email notification: {}", notification);

        // For direct emails like verification, the message is already formatted
        emailService.sendEmail(
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getMessage()
        );
    }

    private String buildEmailBody(NotificationDto notification) {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedAmount = notification.getAmount() != null ?
                currencyFormatter.format(notification.getAmount()) : "0.00";

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(notification.getRecipientName()).append(",\n\n");

        switch (notification.getTransactionType()) {
            case "DEPOSIT":
                body.append("Your account has been credited with ").append(formattedAmount)
                        .append(".\n\nAccount: ").append(notification.getAccountNumber())
                        .append("\nTransaction time: ").append(notification.getTimestamp());
                break;

            case "WITHDRAWAL":
                body.append("Your account has been debited with ").append(formattedAmount)
                        .append(".\n\nAccount: ").append(notification.getAccountNumber())
                        .append("\nTransaction time: ").append(notification.getTimestamp());
                break;

            case "TRANSFER_OUT":
                body.append("You have transferred ").append(formattedAmount)
                        .append(" from your account.\n\nAccount: ").append(notification.getAccountNumber())
                        .append("\nTransaction time: ").append(notification.getTimestamp());
                break;

            case "TRANSFER_IN":
                body.append("You have received ").append(formattedAmount);

                if (notification.getSenderName() != null) {
                    body.append(" from ").append(notification.getSenderName());
                }

                body.append(".\n\nAccount: ").append(notification.getAccountNumber())
                        .append("\nTransaction time: ").append(notification.getTimestamp());
                break;

            default:
                body.append(notification.getMessage());
        }

        body.append("\n\nThank you for using our Banking Service!\n\n")
                .append("Regards,\nMonieBank Team");

        return body.toString();
    }
}