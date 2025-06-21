package com.maxiflexy.auth_service.dto.request;

import com.maxiflexy.auth_service.encryption.Encrypted;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EncryptedSignUpRequest {

    @NotBlank
    @Encrypted
    private String name;

    @NotBlank
    @Email
    @Encrypted
    private String email;

    @NotBlank
    @Size(min = 6, max = 20)
    @Encrypted
    private String password;
}