package com.maxiflexy.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private Long userId;
    private String email;
    private String name;

    public AuthResponse(String accessToken, String refreshToken, Long accessTokenExpiresIn,
                        Long refreshTokenExpiresIn, Long userId, String email, String name) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}