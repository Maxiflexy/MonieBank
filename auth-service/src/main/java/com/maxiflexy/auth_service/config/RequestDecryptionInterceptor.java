package com.maxiflexy.auth_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxiflexy.auth_service.service.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class RequestDecryptionInterceptor implements HandlerInterceptor {

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only process POST and PUT requests that might contain encrypted data
        String method = request.getMethod();
        if ("POST".equals(method) || "PUT".equals(method)) {
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                // Check if request contains encrypted data header
                String isEncrypted = request.getHeader("X-Encrypted-Request");
                if ("true".equals(isEncrypted)) {
                    // Store original request body for later processing
                    // This will be handled by the @RequestBody processing
                    request.setAttribute("isEncryptedRequest", true);
                }
            }
        }
        return true;
    }
}
