package com.maxiflexy.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenRequest {
    @NotBlank
    private String tokenId;
}
