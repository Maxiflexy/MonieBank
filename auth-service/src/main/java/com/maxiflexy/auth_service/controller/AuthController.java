package com.maxiflexy.auth_service.controller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.maxiflexy.auth_service.dto.response.ApiResponse;
import com.maxiflexy.auth_service.dto.response.AuthResponse;
import com.maxiflexy.auth_service.dto.request.LoginRequest;
import com.maxiflexy.auth_service.dto.request.SignUpRequest;
import com.maxiflexy.auth_service.dto.request.GoogleTokenRequest;
import com.maxiflexy.auth_service.dto.request.RefreshTokenRequest;
import com.maxiflexy.auth_service.dto.request.LogoutRequest;
import com.maxiflexy.auth_service.enums.AuthProvider;
import com.maxiflexy.auth_service.exception.ResourceNotFoundException;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import com.maxiflexy.auth_service.service.EmailVerificationService;
import com.maxiflexy.auth_service.service.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private EmailVerificationService emailVerificationService;

    // Get your Google OAuth Client ID from application properties
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, "Email address already in use!"));
        }

        // Create user
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false); // Set as not verified initially

        User savedUser = userRepository.save(user);

        // Send verification email
        emailVerificationService.sendVerificationEmail(savedUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{id}")
                .buildAndExpand(savedUser.getId()).toUri();

        return ResponseEntity
                .created(location)
                .body(new ApiResponse(true, "User registered successfully! Please check your email to verify your account."));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verifies a user's email address via token")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        // Log token receipt for debugging
        System.out.println("Received verification token: " + token);

        boolean verified = emailVerificationService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully. You can now login."));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid or expired verification token."));
        }
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resends the verification email to the user")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getEmailVerified()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email already verified."));
        }

        emailVerificationService.generateNewVerificationToken(user);
        return ResponseEntity.ok(new ApiResponse(true, "Verification email resent. Please check your email."));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Check if email is verified for local users
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

        if (user.getProvider() == AuthProvider.LOCAL && !user.getEmailVerified()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Email not verified. Please verify your email before logging in."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create both access and refresh tokens
        Map<String, Object> tokens = tokenProvider.createTokens(user);

        return ResponseEntity.ok(new AuthResponse(
                (String) tokens.get("accessToken"),
                (String) tokens.get("refreshToken"),
                (Long) tokens.get("accessTokenExpiresIn"),
                (Long) tokens.get("refreshTokenExpiresIn"),
                user.getId(),
                user.getEmail(),
                user.getName()
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();

            // Validate refresh token
            if (!tokenProvider.validateRefreshToken(refreshToken)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Invalid or expired refresh token. Please login again."));
            }

            // Get user from refresh token
            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Blacklist the old refresh token
            tokenProvider.blacklistToken(refreshToken);

            // Create new tokens
            Map<String, Object> tokens = tokenProvider.createTokens(user);

            return ResponseEntity.ok(new AuthResponse(
                    (String) tokens.get("accessToken"),
                    (String) tokens.get("refreshToken"),
                    (Long) tokens.get("accessTokenExpiresIn"),
                    (Long) tokens.get("refreshTokenExpiresIn"),
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            ));

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid refresh token. Please login again."));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and blacklist tokens")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody(required = false) LogoutRequest logoutRequest) {
        try {
            // Extract access token from header
            String accessToken = null;
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                accessToken = bearerToken.substring(7);
            }

            if (accessToken != null && tokenProvider.validateToken(accessToken)) {
                Long userId = tokenProvider.getUserIdFromToken(accessToken);

                // Blacklist access token
                tokenProvider.blacklistToken(accessToken);

                // Blacklist refresh token if provided
                if (logoutRequest != null && logoutRequest.getRefreshToken() != null) {
                    tokenProvider.blacklistToken(logoutRequest.getRefreshToken());
                }

                // If logout from all devices is requested
                if (logoutRequest != null && logoutRequest.isLogoutFromAllDevices()) {
                    tokenProvider.blacklistAllUserTokens(userId);
                }

                return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
            } else {
                return ResponseEntity.ok(new ApiResponse(true, "Already logged out"));
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.ok(new ApiResponse(true, "Logged out"));
        }
    }

    @GetMapping("/validate-token")
    @Operation(summary = "Validate token", description = "Check if token is valid and not blacklisted")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            if (tokenProvider.validateToken(token)) {
                return ResponseEntity.ok(new ApiResponse(true, "Token is valid"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Token is invalid or blacklisted"));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Token validation failed"));
        }
    }

    @PostMapping("/oauth2/google")
    @Operation(summary = "Google OAuth2 login", description = "Processes Google OAuth2 token and returns JWT")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleTokenRequest tokenRequest) {
        try {

            // Verify the Google ID token using Google's API
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            // Verify the token
            GoogleIdToken idToken = verifier.verify(tokenRequest.getTokenId());
            if (idToken == null) {
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Invalid Google token"));
            }

            // Get the payload from the verified token
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Extract user information
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();
            String pictureUrl = (String) payload.get("picture");

            // Check if user exists
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setImageUrl(pictureUrl);
                user.setProvider(AuthProvider.GOOGLE);
                user.setProviderId(googleId);
                user.setEmailVerified(true);

                user = userRepository.save(user);
            } else if (user.getProvider() != AuthProvider.GOOGLE) {
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "You have previously signed up with " + user.getProvider() +
                                ". Please use that method to login."));
            }

            // Generate tokens
            Map<String, Object> tokens = tokenProvider.createTokens(user);

            return ResponseEntity.ok(new AuthResponse(
                    (String) tokens.get("accessToken"),
                    (String) tokens.get("refreshToken"),
                    (Long) tokens.get("accessTokenExpiresIn"),
                    (Long) tokens.get("refreshTokenExpiresIn"),
                    user.getId(),
                    user.getEmail(),
                    user.getName()
            ));
        } catch (Exception e) {
            log.error("Google authentication error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to process Google authentication: " + e.getMessage()));
        }
    }
}