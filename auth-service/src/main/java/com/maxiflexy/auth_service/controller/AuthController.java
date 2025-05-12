package com.maxiflexy.auth_service.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
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

    @PostMapping("/oauth2/google")
    @Operation(summary = "Google OAuth2 login", description = "Processes Google OAuth2 token and returns JWT")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleTokenRequest tokenRequest) {
        // Normally you would verify the token with Google here
        // For this example, we'll simulate the verification process

        // Extract user info from token (in a real app, you'd verify with Google)
        // This is placeholder code - you'd need to implement Google token verification
        Map<String, String> googleUserInfo = parseAndVerifyGoogleToken(tokenRequest.getTokenId());

        String email = googleUserInfo.get("email");
        String name = googleUserInfo.get("name");
        String googleId = googleUserInfo.get("sub");
        String imageUrl = googleUserInfo.get("picture");

        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setImageUrl(imageUrl);
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleId);
            user.setEmailVerified(true); // Google verifies emails

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
    }

    // This is a placeholder - in a real app, you'd verify the token with Google
    private Map<String, String> parseAndVerifyGoogleToken(String tokenId) {
        // In a real implementation, you'd use the GoogleIdTokenVerifier to verify the token
        // For now, we'll simulate a successful verification with dummy data

        // This is just placeholder code - a real implementation would extract this from the verified token
        return Map.of(
                "sub", "1234567890", // Google's unique ID for the user
                "email", "user@example.com",
                "name", "Test User",
                "picture", "https://example.com/photo.jpg"
        );
    }
}