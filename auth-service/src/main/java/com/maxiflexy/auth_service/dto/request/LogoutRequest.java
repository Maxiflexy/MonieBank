package com.maxiflexy.auth_service.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
    private boolean logoutFromAllDevices = false; // Optional: logout from all devices
}