package com.maxiflexy.account_service.dto;

import com.maxiflexy.account_service.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountDto {
    //@NotNull
    private Long userId;

    @NotBlank
    private String fullName;

    @NotBlank
    private String email;

    @NotNull
    private AccountType accountType;
}