package com.maxiflexy.account_service.service;

import com.maxiflexy.account_service.dto.AccountDto;
import com.maxiflexy.account_service.dto.CreateAccountDto;
import com.maxiflexy.account_service.dto.TransferBalanceDto;
//import com.maxiflexy.account_service.exception.InsufficientFundsException;
//import com.maxiflexy.account_service.exception.ResourceNotFoundException;
import com.maxiflexy.account_service.model.Account;
import com.maxiflexy.account_service.repository.AccountRepository;
import com.maxiflexy.common.exception.InsufficientFundsException;
import com.maxiflexy.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserValidationService userValidationService;

    public List<AccountDto> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AccountDto getAccountById(Long userId, Long accountId) {
        log.info("userId from request, {}", userId);
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return convertToDto(account);
    }

    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return convertToDto(account);
    }

    @Transactional
    public AccountDto createAccount(CreateAccountDto createAccountDto) {
        // Validate user exists in auth-service
        if (!userValidationService.validateUserExists(createAccountDto.getUserId())) {
            throw new ResourceNotFoundException("User not found with ID: " + createAccountDto.getUserId());
        }

        Account account = new Account();
        account.setUserId(createAccountDto.getUserId());
        account.setFullName(createAccountDto.getFullName());
        account.setEmail(createAccountDto.getEmail());
        account.setAccountType(createAccountDto.getAccountType());

        Account savedAccount = accountRepository.save(account);
        return convertToDto(savedAccount);
    }

    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUserId(account.getUserId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getAccountType());
        dto.setFullName(account.getFullName());
        dto.setEmail(account.getEmail());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }

    @Transactional
    public AccountDto updateBalance(Long userId, Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);

        return convertToDto(savedAccount);
    }

    @Transactional
    public void transferBetweenAccounts(Long userId, @Valid TransferBalanceDto transferDto) {
        Account fromAccount = accountRepository.findByUserIdAndId(userId, transferDto.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        // Get destination account (no ownership verification required)
        Account toAccount = accountRepository.findById(transferDto.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        // Check for sufficient funds
        if (fromAccount.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(transferDto.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferDto.getAmount()));

        // Save both accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

    }
}