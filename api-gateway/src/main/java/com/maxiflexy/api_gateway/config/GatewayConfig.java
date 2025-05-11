package com.maxiflexy.api_gateway.config;

import com.maxiflexy.api_gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes (Unsecured)
                .route("auth-service-signup", r -> r.path("/api/auth/signup")
                        .uri("lb://auth-service"))
                .route("auth-service-login", r -> r.path("/api/auth/login")
                        .uri("lb://auth-service"))
                .route("auth-service-oauth2", r -> r.path("/api/auth/oauth2/**")
                        .uri("lb://auth-service"))

                // Auth Service Routes (Secured)
                .route("auth-service-secured", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://auth-service"))

                // Account Service Routes (Secured)
                .route("account-service", r -> r.path("/api/accounts/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://account-service"))

                .build();
    }
}