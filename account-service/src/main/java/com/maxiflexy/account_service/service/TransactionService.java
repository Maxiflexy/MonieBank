package com.maxiflexy.account_service.service;

import com.maxiflexy.account_service.dto.*;
import com.maxiflexy.account_service.exception.InsufficientFundsException;
import com.maxiflexy.account_service.exception.ResourceNotFoundException;
import com.maxiflexy.account_service.model.Account;
import com.maxiflexy.account_service.model.Transaction;
import com.maxiflexy.account_service.enums.TransactionStatus;
import com.maxiflexy.account_service.enums.TransactionType;
import com.maxiflexy.account_service.repository.AccountRepository;
import com.maxiflexy.account_service.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KafkaTemplate<String, NotificationDto> kafkaTemplate;

    @Transactional
    public TransactionDto deposit(Long userId, DepositDto depositDto) {

        Account account = accountRepository.findById(depositDto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

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

        // Update account balance
        account.setBalance(account.getBalance().add(depositDto.getAmount()));
        accountRepository.save(account);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Send notification
        sendDepositNotification(account, depositDto.getAmount());

        return convertToDto(savedTransaction);
    }

    @Transactional
    public TransactionDto withdraw(Long userId, WithdrawDto withdrawDto) {
        Account account = accountRepository.findById(withdrawDto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

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

        // Update account balance
        account.setBalance(account.getBalance().subtract(withdrawDto.getAmount()));
        accountRepository.save(account);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Send notification
        sendWithdrawalNotification(account, withdrawDto.getAmount());

        return convertToDto(savedTransaction);
    }

    @Transactional
    public TransactionDto transfer(Long userId, TransferDto transferDto) {
        // Get source account
        Account fromAccount = accountRepository.findById(transferDto.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        // Verify user owns the source account
        if (!fromAccount.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Source account not found for this user");
        }

        // Get destination account
        Account toAccount = accountRepository.findByAccountNumber(transferDto.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

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

        // Update account balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(transferDto.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferDto.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction savedOutgoingTransaction = transactionRepository.save(outgoingTransaction);
        transactionRepository.save(incomingTransaction);

        // Send notifications
        sendTransferNotification(fromAccount, toAccount, transferDto.getAmount());

        return convertToDto(savedOutgoingTransaction);
    }

    public Page<TransactionDto> getTransactionHistory(Long userId, Long accountId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(this::convertToDto);
    }

    public Page<TransactionDto> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable);
        return transactions.map(this::convertToDto);
    }

    private void sendDepositNotification(Account account, BigDecimal amount) {
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

    private void sendWithdrawalNotification(Account account, BigDecimal amount) {
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

    private void sendTransferNotification(Account fromAccount, Account toAccount, BigDecimal amount) {
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
