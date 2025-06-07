package com.maxiflexy.auth_service.dto.response;

import com.maxiflexy.auth_service.encryption.Encrypted;
import com.maxiflexy.auth_service.encryption.EncryptedId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EncryptedAuthResponse {
    private String tokenType = "Bearer";
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;

    @EncryptedId
    private Long userId;

    @Encrypted
    private String email;

    @Encrypted
    private String name;

    private String imageUrl; // This might not need encryption

    public EncryptedAuthResponse(Long accessTokenExpiresIn, Long refreshTokenExpiresIn,
                                 Long userId, String email, String name) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}