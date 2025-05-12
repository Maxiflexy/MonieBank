package com.maxiflexy.auth_service.service;

import com.maxiflexy.auth_service.dto.EmailNotificationDto;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSender notificationSender;

    @Value("${app.auth.verification.token-expiry-minutes}")
    private long tokenExpiryMinutes;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        userRepository.save(user);

        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token;
        String emailBody = "Dear " + user.getName() + ",\n\n"
                + "Please verify your email by clicking the link below:\n\n"
                + verificationUrl + "\n\n"
                + "The link will expire in " + tokenExpiryMinutes + " minutes.\n\n"
                + "Regards,\nMonieBank Team";

        EmailNotificationDto notification = new EmailNotificationDto();
        notification.setRecipientEmail(user.getEmail());
        notification.setRecipientName(user.getName());
        notification.setSubject("Verify Your Email Address");
        notification.setMessage(emailBody);
        notification.setNotificationType("EMAIL_VERIFICATION");

        notificationSender.sendEmailNotification(notification);
    }

    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return false;
        }

        // Check if token is expired
        if (user.getVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);
        userRepository.save(user);

        return true;
    }

    public void generateNewVerificationToken(User user) {
        // Generate new token and send verification email again
        sendVerificationEmail(user);
    }
}