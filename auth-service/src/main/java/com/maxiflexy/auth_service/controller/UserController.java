package com.maxiflexy.auth_service.controller;

import com.maxiflexy.auth_service.dto.response.ApiResponse;
import com.maxiflexy.auth_service.dto.request.ProfileUpdateRequest;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import com.maxiflexy.auth_service.service.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth/user")
@Tag(name = "User", description = "User API")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    // Cookie name (must match with AuthController)
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the current authenticated user from cookie")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // Try to get token from Authorization header first (backward compatibility)
            String token = getTokenFromAuthHeader(request);

            // If no Authorization header, try to get from cookies
            if (token == null) {
                token = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
            }

            if (token == null || !tokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No valid authentication found"));
            }

            Long userId = tokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid authentication"));
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a user by ID - for internal service validation")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Return a minimal user object with just the necessary fields
        User minimalUser = new User();
        minimalUser.setId(user.getId());
        minimalUser.setName(user.getName());
        minimalUser.setEmail(user.getEmail());

        return ResponseEntity.ok(minimalUser);
    }

    @PutMapping("/update")
    @Operation(summary = "Update user profile", description = "Updates the current user's profile")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody ProfileUpdateRequest updateRequest) {

        try {
            // Try to get token from Authorization header first (backward compatibility)
            String token = getTokenFromAuthHeader(request);

            // If no Authorization header, try to get from cookies
            if (token == null) {
                token = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
            }

            if (token == null || !tokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No valid authentication found"));
            }

            Long userId = tokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setName(updateRequest.getName());
            if (updateRequest.getContactAddress() != null) {
                user.setContactAddress(updateRequest.getContactAddress());
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid authentication or update failed"));
        }
    }

    // Helper methods
    private String getTokenFromAuthHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}