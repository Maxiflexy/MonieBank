package com.maxiflexy.auth_service.dto.response;

import com.maxiflexy.auth_service.encryption.Encrypted;
import com.maxiflexy.auth_service.encryption.EncryptedId;
import com.maxiflexy.auth_service.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EncryptedMinimalUserResponse {
    @EncryptedId
    private Long id;

    @Encrypted
    private String name;

    @Encrypted
    private String email;

    public static EncryptedMinimalUserResponse fromUser(User user) {
        EncryptedMinimalUserResponse response = new EncryptedMinimalUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }
}