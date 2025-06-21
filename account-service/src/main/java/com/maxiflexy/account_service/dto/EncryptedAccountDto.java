
// File: account-service/src/main/java/com/maxiflexy/account_service/dto/EncryptedAccountDto.java
package com.maxiflexy.account_service.dto;

import com.maxiflexy.account_service.encryption.Encrypted;
import com.maxiflexy.account_service.encryption.EncryptedAmount;
import com.maxiflexy.account_service.encryption.EncryptedId;
import com.maxiflexy.account_service.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedAccountDto {
    @EncryptedId
    private Long id;

    @EncryptedId
    private Long userId;

    @Encrypted
    private String accountNumber;

    @EncryptedAmount
    private BigDecimal balance;

    private AccountType accountType;

    @Encrypted
    private String fullName;

    @Encrypted
    private String email;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method to convert from regular AccountDto
    public static EncryptedAccountDto fromAccountDto(AccountDto accountDto) {
        EncryptedAccountDto encrypted = new EncryptedAccountDto();
        encrypted.setId(accountDto.getId());
        encrypted.setUserId(accountDto.getUserId());
        encrypted.setAccountNumber(accountDto.getAccountNumber());
        encrypted.setBalance(accountDto.getBalance());
        encrypted.setAccountType(accountDto.getAccountType());
        encrypted.setFullName(accountDto.getFullName());
        encrypted.setEmail(accountDto.getEmail());
        encrypted.setCreatedAt(accountDto.getCreatedAt());
        encrypted.setUpdatedAt(accountDto.getUpdatedAt());
        return encrypted;
    }
}