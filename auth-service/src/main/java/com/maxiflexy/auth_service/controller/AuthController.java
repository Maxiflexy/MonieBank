package com.maxiflexy.auth_service.controller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.maxiflexy.auth_service.dto.response.ApiResponse;
import com.maxiflexy.auth_service.dto.response.AuthResponse;
import com.maxiflexy.auth_service.dto.request.LoginRequest;
import com.maxiflexy.auth_service.dto.request.SignUpRequest;
import com.maxiflexy.auth_service.dto.request.GoogleTokenRequest;
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
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
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
        String token = tokenProvider.createToken(user);

        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName()));
    }

    // In auth-service/src/main/java/com/maxiflexy/auth_service/controller/AuthController.java

    // In auth-service/src/main/java/com/maxiflexy/auth_service/controller/AuthController.java

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

            // Generate token
            String token = tokenProvider.createToken(user);

            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getName()));
        } catch (Exception e) {
            log.error("Google authentication error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to process Google authentication: " + e.getMessage()));
        }
    }
}