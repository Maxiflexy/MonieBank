package com.maxiflexy.account_service.dto;

import com.maxiflexy.account_service.enums.TransactionStatus;
import com.maxiflexy.account_service.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long userId;
    private Long accountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
}
