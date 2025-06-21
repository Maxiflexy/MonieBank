package com.maxiflexy.auth_service.dto.request;

import com.maxiflexy.auth_service.encryption.Encrypted;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EncryptedLoginRequest {

    @NotBlank
    @Email
    @Encrypted
    private String email;

    @NotBlank
    @Encrypted
    private String password;
}