package com.maxiflexy.transaction_service.service;

import com.maxiflexy.common.exception.InsufficientFundsException;
import com.maxiflexy.common.exception.ResourceNotFoundException;
import com.maxiflexy.transaction_service.dto.AccountDto;
import com.maxiflexy.transaction_service.dto.TransferBalanceDto;
//import com.maxiflexy.transaction_service.exception.InsufficientFundsException;
//import com.maxiflexy.transaction_service.exception.ResourceNotFoundException;
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

    public void transferBetweenAccounts(Long userId, Long fromAccountId, Long toAccountId, BigDecimal amount) {
        TransferBalanceDto transferDto = new TransferBalanceDto(fromAccountId, toAccountId, amount);

        webClientBuilder.build()
                .put()
                .uri("lb://account-service/api/accounts/transfer")
                .header("X-User-Id", userId.toString())
                .bodyValue(transferDto)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                                return Mono.error(new ResourceNotFoundException("One of the accounts was not found"));
                            } else if (clientResponse.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                                return Mono.error(new InsufficientFundsException("Insufficient funds for transfer"));
                            }
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("Error during transfer: " + body)));
                        })
                .bodyToMono(Void.class)
                .block();
    }
}