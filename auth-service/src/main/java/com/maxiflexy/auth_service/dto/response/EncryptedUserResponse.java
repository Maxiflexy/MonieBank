package com.maxiflexy.auth_service.dto.response;

import com.maxiflexy.auth_service.encryption.Encrypted;
import com.maxiflexy.auth_service.encryption.EncryptedId;
import com.maxiflexy.auth_service.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EncryptedUserResponse {
    @EncryptedId
    private Long id;

    @Encrypted
    private String name;

    @Encrypted
    private String email;

    private String imageUrl;
    private Boolean emailVerified;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EncryptedUserResponse fromUser(User user) {
        EncryptedUserResponse response = new EncryptedUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setImageUrl(user.getImageUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setProvider(user.getProvider() != null ? user.getProvider().toString() : null);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
