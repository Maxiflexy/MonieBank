package com.maxiflexy.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDto {
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String message;
    private String notificationType;
}