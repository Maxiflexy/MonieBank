package com.maxiflexy.auth_service.controller;

import com.maxiflexy.auth_service.dto.response.ApiResponse;
import com.maxiflexy.auth_service.dto.request.ProfileUpdateRequest;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import com.maxiflexy.auth_service.service.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/user")
@Tag(name = "User", description = "User API")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the current authenticated user")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                return ResponseEntity.ok(user);
            }
        }

        return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid token"));
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
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody ProfileUpdateRequest request) {

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                user.setName(request.getName());
                if (request.getContactAddress() != null) {
                    user.setContactAddress(request.getContactAddress());
                }

                User updatedUser = userRepository.save(user);
                return ResponseEntity.ok(updatedUser);
            }
        }

        return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid token"));
    }
}