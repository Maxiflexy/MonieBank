package com.maxiflexy.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    // NO TOKENS - They are in HTTP-only cookies only
    private String tokenType = "Bearer";
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;
    private Long userId;
    private String email;
    private String name;
    private String imageUrl;

    public AuthResponse(Long accessTokenExpiresIn, Long refreshTokenExpiresIn,
                        Long userId, String email, String name) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}