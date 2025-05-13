package com.maxiflexy.transaction_service.service;

import com.maxiflexy.transaction_service.dto.AccountDto;
import com.maxiflexy.transaction_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class AccountService {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public AccountDto getAccountById(Long userId, Long accountId) {
        return webClientBuilder.build()
                .get()
                .uri("lb://account-service/api/accounts/{accountId}", accountId)
                .header("X-User-Id", userId.toString())
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        clientResponse -> Mono.error(new ResourceNotFoundException("Account not found with ID: " + accountId))
                )
                .bodyToMono(AccountDto.class)
                .block();
    }

    public AccountDto getAccountByNumber(Long userId, String accountNumber) {
        return webClientBuilder.build()
                .get()
                .uri("lb://account-service/api/accounts/number/{accountNumber}", accountNumber)
                .header("X-User-Id", userId.toString())
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        clientResponse -> Mono.error(new ResourceNotFoundException("Account not found with number: " + accountNumber))
                )
                .bodyToMono(AccountDto.class)
                .block();
    }

    public void updateBalance(Long userId, Long accountId, BigDecimal newBalance) {
        webClientBuilder.build()
                .put()
                .uri("lb://account-service/api/accounts/{id}/balance", accountId)
                .header("X-User-Id", userId.toString())
                .bodyValue(newBalance)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        clientResponse -> Mono.error(new ResourceNotFoundException("Account not found with ID: " + accountId))
                )
                .bodyToMono(Void.class)
                .block();
    }
}