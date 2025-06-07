package com.maxiflexy.transaction_service.dto;

//import com.maxiflexy.transaction_service.encryption.EncryptedId;
import com.maxiflexy.transaction_service.enums.TransactionStatus;
import com.maxiflexy.common.encryption.annotations.EncryptedId;
import com.maxiflexy.transaction_service.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedTransactionDto {
    @EncryptedId
    private Long id;

    @EncryptedId
    private Long userId;

    // Note: We might not want to encrypt accountId and targetAccountId
    // as they might be needed for internal operations
    // But if you want to encrypt them, add @EncryptedId annotation
    private Long accountId;
    private Long targetAccountId;

    private BigDecimal amount; // You can add @EncryptedAmount if needed
    private TransactionType type;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;

    // Factory method to convert from regular TransactionDto
    public static EncryptedTransactionDto fromTransactionDto(TransactionDto transactionDto) {
        EncryptedTransactionDto encrypted = new EncryptedTransactionDto();
        encrypted.setId(transactionDto.getId());
        encrypted.setUserId(transactionDto.getUserId());
        encrypted.setAccountId(transactionDto.getAccountId());
        encrypted.setTargetAccountId(transactionDto.getTargetAccountId());
        encrypted.setAmount(transactionDto.getAmount());
        encrypted.setType(transactionDto.getType());
        encrypted.setDescription(transactionDto.getDescription());
        encrypted.setStatus(transactionDto.getStatus());
        encrypted.setTransactionDate(transactionDto.getTransactionDate());
        return encrypted;
    }
}