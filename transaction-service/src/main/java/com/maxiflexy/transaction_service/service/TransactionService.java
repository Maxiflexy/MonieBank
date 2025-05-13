package com.maxiflexy.transaction_service.service;


import com.maxiflexy.transaction_service.dto.*;
import com.maxiflexy.transaction_service.enums.TransactionStatus;
import com.maxiflexy.transaction_service.enums.TransactionType;
import com.maxiflexy.transaction_service.exception.InsufficientFundsException;
import com.maxiflexy.transaction_service.exception.ResourceNotFoundException;
import com.maxiflexy.transaction_service.model.Transaction;
import com.maxiflexy.transaction_service.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Transactional
    public TransactionDto deposit(Long userId, DepositDto depositDto) {
        // Get account from account service

        log.info("userId from request, {}", userId);
        log.info("Deposit request, {}", depositDto);
        AccountDto account = accountService.getAccountById(depositDto.getAccountId());

        // Verify user owns the account
        if (!account.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found for this user");
        }

        // Create a transaction record
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(account.getId());
        transaction.setAmount(depositDto.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDescription(depositDto.getDescription());
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Update account balance via account service
        BigDecimal newBalance = account.getBalance().add(depositDto.getAmount());
        accountService.updateBalance(account.getId(), newBalance);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Send notification
        sendDepositNotification(account, depositDto.getAmount());

        return convertToDto(savedTransaction);
    }

    @Transactional
    public TransactionDto withdraw(Long userId, WithdrawDto withdrawDto) {
        // Get account from account service
        AccountDto account = accountService.getAccountById(withdrawDto.getAccountId());

        // Verify user owns the account
        if (!account.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found for this user");
        }

        // Check if sufficient funds
        if (account.getBalance().compareTo(withdrawDto.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Create a transaction record
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAccountId(account.getId());
        transaction.setAmount(withdrawDto.getAmount());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setDescription(withdrawDto.getDescription());
        transaction.setStatus(TransactionStatus.COMPLETED);

        // Update account balance via account service
        BigDecimal newBalance = account.getBalance().subtract(withdrawDto.getAmount());
        accountService.updateBalance(account.getId(), newBalance);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Send notification
        sendWithdrawalNotification(account, withdrawDto.getAmount());

        return convertToDto(savedTransaction);
    }

    @Transactional
    public TransactionDto transfer(Long userId, TransferDto transferDto) {
        // Get source account from account service
        AccountDto fromAccount = accountService.getAccountById(transferDto.getFromAccountId());

        // Verify user owns the source account
        if (!fromAccount.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Source account not found for this user");
        }

        // Get destination account from account service
        AccountDto toAccount = accountService.getAccountByNumber(transferDto.getToAccountNumber());

        // Check if sufficient funds
        if (fromAccount.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Create outgoing transaction
        Transaction outgoingTransaction = new Transaction();
        outgoingTransaction.setUserId(userId);
        outgoingTransaction.setAccountId(fromAccount.getId());
        outgoingTransaction.setTargetAccountId(toAccount.getId());
        outgoingTransaction.setAmount(transferDto.getAmount());
        outgoingTransaction.setType(TransactionType.TRANSFER_OUT);
        outgoingTransaction.setDescription(transferDto.getDescription());
        outgoingTransaction.setStatus(TransactionStatus.COMPLETED);

        // Create incoming transaction
        Transaction incomingTransaction = new Transaction();
        incomingTransaction.setUserId(toAccount.getUserId());
        incomingTransaction.setAccountId(toAccount.getId());
        incomingTransaction.setTargetAccountId(fromAccount.getId());
        incomingTransaction.setAmount(transferDto.getAmount());
        incomingTransaction.setType(TransactionType.TRANSFER_IN);
        incomingTransaction.setDescription("Transfer from " + fromAccount.getAccountNumber());
        incomingTransaction.setStatus(TransactionStatus.COMPLETED);

        // Update account balances via account service
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(transferDto.getAmount());
        BigDecimal newToBalance = toAccount.getBalance().add(transferDto.getAmount());

        accountService.updateBalance(fromAccount.getId(), newFromBalance);
        accountService.updateBalance(toAccount.getId(), newToBalance);

        Transaction savedOutgoingTransaction = transactionRepository.save(outgoingTransaction);
        transactionRepository.save(incomingTransaction);

        // Send notifications
        sendTransferNotification(fromAccount, toAccount, transferDto.getAmount());

        return convertToDto(savedOutgoingTransaction);
    }

    public Page<TransactionDto> getTransactionHistory(Long userId, Long accountId, Pageable pageable) {
        // First validate that the account belongs to the user
        AccountDto account = accountService.getAccountById(accountId);
        if (!account.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found for this user");
        }

        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(this::convertToDto);
    }

    public Page<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable);
        return transactions.map(this::convertToDto);
    }

    private void sendDepositNotification(AccountDto account, BigDecimal amount) {
        NotificationDto notification = new NotificationDto();
        notification.setRecipientEmail(account.getEmail());
        notification.setRecipientName(account.getFullName());
        notification.setSubject("Deposit Successful");
        notification.setMessage("Your account has been credited with " + amount);
        notification.setAccountNumber(account.getAccountNumber());
        notification.setAmount(amount);
        notification.setTransactionType("DEPOSIT");
        notification.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send("transaction-notifications", notification);
    }

    private void sendWithdrawalNotification(AccountDto account, BigDecimal amount) {
        NotificationDto notification = new NotificationDto();
        notification.setRecipientEmail(account.getEmail());
        notification.setRecipientName(account.getFullName());
        notification.setSubject("Withdrawal Successful");
        notification.setMessage("Your account has been debited with " + amount);
        notification.setAccountNumber(account.getAccountNumber());
        notification.setAmount(amount);
        notification.setTransactionType("WITHDRAWAL");
        notification.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send("transaction-notifications", notification);
    }

    private void sendTransferNotification(AccountDto fromAccount, AccountDto toAccount, BigDecimal amount) {
        // Sender notification
        NotificationDto senderNotification = new NotificationDto();
        senderNotification.setRecipientEmail(fromAccount.getEmail());
        senderNotification.setRecipientName(fromAccount.getFullName());
        senderNotification.setSubject("Transfer Successful");
        senderNotification.setMessage("You have transferred " + amount + " to account " + toAccount.getAccountNumber());
        senderNotification.setAccountNumber(fromAccount.getAccountNumber());
        senderNotification.setAmount(amount);
        senderNotification.setTransactionType("TRANSFER_OUT");
        senderNotification.setTimestamp(LocalDateTime.now());

        // Recipient notification
        NotificationDto recipientNotification = new NotificationDto();
        recipientNotification.setRecipientEmail(toAccount.getEmail());
        recipientNotification.setRecipientName(toAccount.getFullName());
        recipientNotification.setSenderName(fromAccount.getFullName());
        recipientNotification.setSubject("Transfer Received");
        recipientNotification.setMessage("You have received " + amount + " from account " + fromAccount.getAccountNumber());
        recipientNotification.setAccountNumber(toAccount.getAccountNumber());
        recipientNotification.setAmount(amount);
        recipientNotification.setTransactionType("TRANSFER_IN");
        recipientNotification.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send("transaction-notifications", senderNotification);
        kafkaTemplate.send("transaction-notifications", recipientNotification);
    }

    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setAccountId(transaction.getAccountId());
        dto.setTargetAccountId(transaction.getTargetAccountId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        dto.setTransactionDate(transaction.getTransactionDate());
        return dto;
    }
}
