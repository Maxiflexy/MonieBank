package com.maxiflexy.transaction_service.controller;

import com.maxiflexy.transaction_service.dto.*;
import com.maxiflexy.transaction_service.service.TransactionService;
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
@RequestMapping("/api/transactions")
@Tag(name = "Transaction", description = "Banking transaction API")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

//    @PostMapping("/deposit")
//    @Operation(summary = "Deposit funds", description = "Deposits funds into an account")
//    public ResponseEntity<TransactionDto> deposit(
//            @RequestHeader("X-User-Id") Long userId,
//            @Valid @RequestBody DepositDto depositDto) {
//        TransactionDto transaction = transactionService.deposit(userId, depositDto);
//        return ResponseEntity.ok(transaction);
//    }
//
//    @PostMapping("/withdraw")
//    @Operation(summary = "Withdraw funds", description = "Withdraws funds from an account")
//    public ResponseEntity<TransactionDto> withdraw(
//            @RequestHeader("X-User-Id") Long userId,
//            @Valid @RequestBody WithdrawDto withdrawDto) {
//        System.out.println("userId: " +userId + ",....withdrawDto :" + withdrawDto.toString());
//        TransactionDto transaction = transactionService.withdraw(userId, withdrawDto);
//        return ResponseEntity.ok(transaction);
//    }
//
//    @PostMapping("/transfer")
//    @Operation(summary = "Transfer funds", description = "Transfers funds between accounts")
//    public ResponseEntity<TransactionDto> transfer(
//            @RequestHeader("X-User-Id") Long userId,
//            @Valid @RequestBody TransferDto transferDto) {
//        TransactionDto transaction = transactionService.transfer(userId, transferDto);
//        return ResponseEntity.ok(transaction);
//    }
//
//    @GetMapping("/history/{accountId}")
//    @Operation(summary = "Get transaction history", description = "Returns transaction history for an account")
//    public ResponseEntity<Page<TransactionDto>> getTransactionHistory(
//            @RequestHeader("X-User-Id") Long userId,
//            @PathVariable Long accountId,
//            Pageable pageable) {
//        Page<TransactionDto> transactions = transactionService.getTransactionHistory(userId, accountId, pageable);
//        return ResponseEntity.ok(transactions);
//    }
//
//    @GetMapping("/date-range")
//    @Operation(summary = "Get transactions by date range", description = "Returns transactions within a date range")
//    public ResponseEntity<Page<TransactionDto>> getTransactionsByDateRange(
//            @RequestHeader("X-User-Id") Long userId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
//            Pageable pageable) {
//        Page<TransactionDto> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate, pageable);
//        return ResponseEntity.ok(transactions);
//    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds", description = "Deposits funds into an account")
    public ResponseEntity<EncryptedTransactionDto> deposit(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody DepositDto depositDto) {
        TransactionDto transaction = transactionService.deposit(userId, depositDto);
        EncryptedTransactionDto encryptedTransaction = EncryptedTransactionDto.fromTransactionDto(transaction);
        return ResponseEntity.ok(encryptedTransaction);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds", description = "Withdraws funds from an account")
    public ResponseEntity<EncryptedTransactionDto> withdraw(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WithdrawDto withdrawDto) {
        System.out.println("userId: " +userId + ",....withdrawDto :" + withdrawDto.toString());
        TransactionDto transaction = transactionService.withdraw(userId, withdrawDto);
        EncryptedTransactionDto encryptedTransaction = EncryptedTransactionDto.fromTransactionDto(transaction);
        return ResponseEntity.ok(encryptedTransaction);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfers funds between accounts")
    public ResponseEntity<EncryptedTransactionDto> transfer(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransferDto transferDto) {
        TransactionDto transaction = transactionService.transfer(userId, transferDto);
        EncryptedTransactionDto encryptedTransaction = EncryptedTransactionDto.fromTransactionDto(transaction);
        return ResponseEntity.ok(encryptedTransaction);
    }

    @GetMapping("/history/{accountId}")
    @Operation(summary = "Get transaction history", description = "Returns transaction history for an account")
    public ResponseEntity<Page<EncryptedTransactionDto>> getTransactionHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long accountId,
            Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getTransactionHistory(userId, accountId, pageable);
        Page<EncryptedTransactionDto> encryptedTransactions = transactions.map(EncryptedTransactionDto::fromTransactionDto);
        return ResponseEntity.ok(encryptedTransactions);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get transactions by date range", description = "Returns transactions within a date range")
    public ResponseEntity<Page<EncryptedTransactionDto>> getTransactionsByDateRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<TransactionDto> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate, pageable);
        Page<EncryptedTransactionDto> encryptedTransactions = transactions.map(EncryptedTransactionDto::fromTransactionDto);
        return ResponseEntity.ok(encryptedTransactions);
    }
}
