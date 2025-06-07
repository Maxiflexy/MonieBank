package com.maxiflexy.auth_service.dto.request;

//import com.maxiflexy.auth_service.encryption.Encrypted;
import com.maxiflexy.common.encryption.annotations.Encrypted;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank
    @Encrypted
    private String name;

    @Encrypted
    private String contactAddress;
}