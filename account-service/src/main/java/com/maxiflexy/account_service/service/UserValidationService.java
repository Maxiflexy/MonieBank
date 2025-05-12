package com.maxiflexy.account_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserValidationService {

    private static final Logger logger = LoggerFactory.getLogger(UserValidationService.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    public boolean validateUserExists(Long userId) {
        try {
            return Boolean.TRUE.equals(webClientBuilder.build()
                    .get()
                    .uri("lb://auth-service/api/auth/user/{userId}", userId)
                    .retrieve()
                    .onStatus(
                            status -> status.equals(HttpStatus.NOT_FOUND),
                            clientResponse -> Mono.just(new RuntimeException("User not found"))
                    )
                    .bodyToMono(Object.class)
                    .map(response -> true)
                    .onErrorResume(e -> {
                        logger.error("Error validating user: {}", e.getMessage());
                        return Mono.just(false);
                    })
                    .block());
        } catch (Exception e) {
            logger.error("Failed to validate user: {}", e.getMessage());
            return false;
        }
    }
}
