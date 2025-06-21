package com.maxiflexy.auth_service.dto.request;

import com.maxiflexy.auth_service.encryption.Encrypted;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EncryptedProfileUpdateRequest {
    @NotBlank
    @Encrypted
    private String name;

    @Encrypted
    private String contactAddress;
}