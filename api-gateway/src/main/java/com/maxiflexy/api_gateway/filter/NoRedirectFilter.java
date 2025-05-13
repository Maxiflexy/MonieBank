package com.maxiflexy.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class NoRedirectFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getHeaders().containsKey("X-No-Redirect")) {
            // Add an attribute to the exchange to indicate no redirection should occur
            exchange.getAttributes().put("noRedirect", Boolean.TRUE);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Execute before other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
