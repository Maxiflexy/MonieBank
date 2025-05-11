package com.maxiflexy.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String name;

    public AuthResponse(String accessToken, Long userId, String email, String name) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}
