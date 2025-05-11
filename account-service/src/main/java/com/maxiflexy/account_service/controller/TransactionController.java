package com.maxiflexy.account_service.controller;

import com.maxiflexy.account_service.dto.*;
import com.maxiflexy.account_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/accounts/transactions")
@Tag(name = "Transaction", description = "Banking transaction API")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds", description = "Deposits funds into an account")
    public ResponseEntity<TransactionDto> deposit(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DepositDto depositDto) {
        TransactionDto transaction = transactionService.deposit(userId, depositDto);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds", description = "Withdraws funds from an account")
    public ResponseEntity<TransactionDto> withdraw(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WithdrawDto withdrawDto) {
        TransactionDto transaction = transactionService.withdraw(userId, withdrawDto);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfers funds between accounts")
    public ResponseEntity<TransactionDto> transfer(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransferDto transferDto) {
        TransactionDto transaction = transactionService.transfer(userId, transferDto);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/history/{accountId}")
    @Operation(summary = "Get transaction history", description = "Returns transaction history for an account")
    public ResponseEntity<Page<TransactionDto>> getTransactionHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long accountId,
            Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getTransactionHistory(userId, accountId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Returns transactions within a date range")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByDateRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }
}
