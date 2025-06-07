package com.maxiflexy.account_service.controller;

import com.maxiflexy.account_service.dto.AccountDto;
import com.maxiflexy.account_service.dto.CreateAccountDto;
import com.maxiflexy.account_service.dto.EncryptedAccountDto;
import com.maxiflexy.account_service.dto.TransferBalanceDto;
import com.maxiflexy.account_service.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account", description = "Account management API")
public class AccountController {

    @Autowired
    private AccountService accountService;

//    @GetMapping
//    @Operation(summary = "Get all accounts for a user", description = "Returns a list of accounts owned by the user")
//    public ResponseEntity<List<AccountDto>> getAccounts(@RequestHeader("X-User-Id") Long userId) {
//        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);
//        return ResponseEntity.ok(accounts);
//    }
//
//    @GetMapping("/{accountId}")
//    @Operation(summary = "Get account by ID", description = "Returns an account by its ID")
//    public ResponseEntity<AccountDto> getAccountById(
//            @RequestHeader("X-User-Id") Long userId,
//            @PathVariable Long accountId) {
//        AccountDto account = accountService.getAccountById(userId, accountId);
//        return ResponseEntity.ok(account);
//    }
//
//    @GetMapping("/number/{accountNumber}")
//    @Operation(summary = "Get account by number", description = "Returns an account by its account number")
//    public ResponseEntity<AccountDto> getAccountByNumber(@PathVariable String accountNumber) {
//        AccountDto account = accountService.getAccountByNumber(accountNumber);
//        return ResponseEntity.ok(account);
//    }
//
//    @PostMapping
//    @Operation(summary = "Create a new account", description = "Creates a new account for the user")
//    public ResponseEntity<AccountDto> createAccount(
//            @RequestHeader("X-User-Id") Long userId,
//            @Valid @RequestBody CreateAccountDto createAccountDto) {
//        // Ensure the userId in the request body matches the one in the header
//        System.out.println("extracted ID: " +userId.toString());
//        createAccountDto.setUserId(userId);
//        AccountDto createdAccount = accountService.createAccount(createAccountDto);
//        return ResponseEntity.ok(createdAccount);
//    }
//
//    @PutMapping("/{accountId}/balance")
//    @Operation(summary = "Update account balance", description = "Updates the balance of an account")
//    public ResponseEntity<AccountDto> updateBalance(
//            @RequestHeader("X-User-Id") Long userId,
//            @PathVariable Long accountId,
//            @RequestBody BigDecimal newBalance) {
//        AccountDto updatedAccount = accountService.updateBalance(userId, accountId, newBalance);
//        return ResponseEntity.ok(updatedAccount);
//    }

    @PutMapping("/transfer")
    @Operation(summary = "Transfer between accounts", description = "Updates balances for two accounts in a transfer")
    public ResponseEntity<Void> transferBetweenAccounts(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransferBalanceDto transferDto) {
        accountService.transferBetweenAccounts(userId, transferDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all accounts for a user", description = "Returns a list of accounts owned by the user")
    public ResponseEntity<List<EncryptedAccountDto>> getAccounts(@RequestHeader("X-User-Id") Long userId) {
        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);
        List<EncryptedAccountDto> encryptedAccounts = accounts.stream()
                .map(EncryptedAccountDto::fromAccountDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(encryptedAccounts);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Returns an account by its ID")
    public ResponseEntity<EncryptedAccountDto> getAccountById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long accountId) {
        AccountDto account = accountService.getAccountById(userId, accountId);
        EncryptedAccountDto encryptedAccount = EncryptedAccountDto.fromAccountDto(account);
        return ResponseEntity.ok(encryptedAccount);
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by number", description = "Returns an account by its account number")
    public ResponseEntity<EncryptedAccountDto> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        EncryptedAccountDto encryptedAccount = EncryptedAccountDto.fromAccountDto(account);
        return ResponseEntity.ok(encryptedAccount);
    }

    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new account for the user")
    public ResponseEntity<EncryptedAccountDto> createAccount(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateAccountDto createAccountDto) {
        createAccountDto.setUserId(userId);
        AccountDto createdAccount = accountService.createAccount(createAccountDto);
        EncryptedAccountDto encryptedAccount = EncryptedAccountDto.fromAccountDto(createdAccount);
        return ResponseEntity.ok(encryptedAccount);
    }

    @PutMapping("/{accountId}/balance")
    @Operation(summary = "Update account balance", description = "Updates the balance of an account")
    public ResponseEntity<EncryptedAccountDto> updateBalance(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long accountId,
            @RequestBody BigDecimal newBalance) {
        AccountDto updatedAccount = accountService.updateBalance(userId, accountId, newBalance);
        EncryptedAccountDto encryptedAccount = EncryptedAccountDto.fromAccountDto(updatedAccount);
        return ResponseEntity.ok(encryptedAccount);
    }
}
