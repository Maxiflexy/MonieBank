package com.maxiflexy.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    @Value("${app.auth.tokenSecret}")
    private String tokenSecret;

    private final WebClient.Builder webClientBuilder;

    // Cookie name (must match with AuthController)
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Get token from cookies ONLY (no Authorization header support)
        String token = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);

        if (token == null) {
            System.out.println("No access token cookie found");
            return onError(exchange, "Authentication required. Please login.", HttpStatus.UNAUTHORIZED);
        }

        System.out.println("Processing access token from cookie");

        // Validate token format first
        if (!isJwtValid(token)) {
            System.out.println("Invalid token format!");
            return onError(exchange, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
        }

        // Check if token is blacklisted by calling auth service
        return checkTokenBlacklist(token)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        System.out.println("Token is blacklisted!");
                        return onError(exchange, "Authentication expired. Please login again.", HttpStatus.UNAUTHORIZED);
                    }

                    // Add user id and email to request headers
                    Claims claims = getClaims(token);
                    System.out.println("Authenticated user ID: " + claims.getSubject());

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", claims.getSubject())
                            .header("X-User-Email", claims.get("email", String.class))
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(throwable -> {
                    System.out.println("Error during token validation: " + throwable.getMessage());
                    return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
                });
    }

    private String getTokenFromCookie(ServerHttpRequest request, String cookieName) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null && cookies.containsKey(cookieName)) {
            HttpCookie cookie = cookies.getFirst(cookieName);
            return cookie != null ? cookie.getValue() : null;
        }
        return null;
    }

    private Mono<Boolean> checkTokenBlacklist(String token) {
        return webClientBuilder.build()
                .get()
                .uri("lb://auth-service/api/auth/validate-token?token=" + token)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        // Token is valid (not blacklisted)
                        return response.bodyToMono(String.class).map(body -> false);
                    } else if (response.statusCode().equals(HttpStatus.UNAUTHORIZED) ||
                            response.statusCode().equals(HttpStatus.FORBIDDEN)) {
                        // Token is blacklisted or invalid
                        return Mono.just(true);
                    } else {
                        // Other error, assume token is invalid
                        return Mono.just(true);
                    }
                })
                .onErrorReturn(true); // On error, assume token is invalid
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        // Add helpful headers for frontend
        response.getHeaders().add("X-Auth-Error", err);
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");

        System.out.println("Authentication error: " + err);
        return response.setComplete();
    }

    private boolean isJwtValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
            getClaims(token);
            return true;
        } catch (Exception ex) {
            System.out.println("Token validation error: " + ex.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}