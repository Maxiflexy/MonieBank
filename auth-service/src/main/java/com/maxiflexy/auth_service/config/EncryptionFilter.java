package com.maxiflexy.auth_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class EncryptionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check if client supports encryption
            String clientSupportsEncryption = httpRequest.getHeader("X-Supports-Encryption");
            String requestEncrypted = httpRequest.getHeader("X-Request-Encrypted");

            // Set context for this request
            EncryptionContext.setEncryptionEnabled("true".equals(clientSupportsEncryption));
            EncryptionContext.setDecryptionEnabled("true".equals(requestEncrypted));

            // Add response header to indicate server supports encryption
            httpResponse.setHeader("X-Server-Supports-Encryption", "true");
            if (EncryptionContext.isEncryptionEnabled()) {
                httpResponse.setHeader("X-Response-Encrypted", "true");
            }

            chain.doFilter(request, response);
        } finally {
            // Clean up context
            EncryptionContext.clear();
        }
    }
}