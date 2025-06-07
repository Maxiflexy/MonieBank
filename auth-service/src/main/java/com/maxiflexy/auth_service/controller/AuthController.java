package com.maxiflexy.auth_service.controller;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.maxiflexy.auth_service.dto.response.ApiResponse;
import com.maxiflexy.auth_service.dto.response.AuthResponse;
import com.maxiflexy.auth_service.dto.request.LoginRequest;
import com.maxiflexy.auth_service.dto.request.SignUpRequest;
import com.maxiflexy.auth_service.dto.request.GoogleTokenRequest;
import com.maxiflexy.auth_service.dto.request.LogoutRequest;
import com.maxiflexy.auth_service.dto.response.EncryptedAuthResponse;
import com.maxiflexy.auth_service.enums.AuthProvider;
import com.maxiflexy.auth_service.exception.ResourceNotFoundException;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import com.maxiflexy.auth_service.service.EmailVerificationService;
import com.maxiflexy.auth_service.service.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Arrays;
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

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${app.auth.accessTokenExpirationMsec}")
    private long accessTokenExpirationMsec;

    @Value("${app.auth.refreshTokenExpirationMsec}")
    private long refreshTokenExpirationMsec;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    // Cookie names
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, "Email address already in use!"));
        }

        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
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
        log.info("Received verification token: {}", token);

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

//    @PostMapping("/login")
//    @Operation(summary = "User login", description = "Authenticates a user and sets JWT tokens as HTTP-only cookies")
//    public ResponseEntity<?> authenticateUser(
//            @Valid @RequestBody LoginRequest loginRequest,
//            HttpServletResponse response) {
//
//        User user = userRepository.findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));
//
//        if (user.getProvider() == AuthProvider.LOCAL && !user.getEmailVerified()) {
//            return ResponseEntity
//                    .status(HttpStatus.FORBIDDEN)
//                    .body(new ApiResponse(false, "Email not verified. Please verify your email before logging in."));
//        }
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getEmail(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        Map<String, Object> tokens = tokenProvider.createTokens(user);
//
//        setTokenCookies(response,
//                (String) tokens.get("accessToken"),
//                (String) tokens.get("refreshToken"));
//
//        AuthResponse authResponse = new AuthResponse();
//        authResponse.setUserId(user.getId());
//        authResponse.setEmail(user.getEmail());
//        authResponse.setName(user.getName());
//        authResponse.setImageUrl(user.getImageUrl());
//        authResponse.setTokenType("Bearer");
//        authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
//        authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));
//
//        return ResponseEntity.ok(authResponse);
//    }
//
//    @PostMapping("/refresh")
//    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token from cookies")
//    public ResponseEntity<?> refreshToken(
//            HttpServletRequest request,
//            HttpServletResponse response) {
//        try {
//            String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE);
//
//            if (refreshToken == null) {
//                clearTokenCookies(response);
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "No refresh token found. Please login again."));
//            }
//
//            if (!tokenProvider.validateRefreshToken(refreshToken)) {
//                clearTokenCookies(response);
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "Invalid or expired refresh token. Please login again."));
//            }
//
//            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//            tokenProvider.blacklistToken(refreshToken);
//
//            Map<String, Object> tokens = tokenProvider.createTokens(user);
//
//            setTokenCookies(response,
//                    (String) tokens.get("accessToken"),
//                    (String) tokens.get("refreshToken"));
//
//            AuthResponse authResponse = new AuthResponse();
//            authResponse.setUserId(user.getId());
//            authResponse.setEmail(user.getEmail());
//            authResponse.setName(user.getName());
//            authResponse.setImageUrl(user.getImageUrl());
//            authResponse.setTokenType("Bearer");
//            authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
//            authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));
//
//            return ResponseEntity.ok(authResponse);
//
//        } catch (Exception e) {
//            log.error("Error refreshing token: {}", e.getMessage());
//            clearTokenCookies(response);
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse(false, "Invalid refresh token. Please login again."));
//        }
//    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and clear cookies")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) LogoutRequest logoutRequest) {
        try {
            String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
            String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE);

            if (accessToken != null && tokenProvider.validateToken(accessToken)) {
                Long userId = tokenProvider.getUserIdFromToken(accessToken);

                tokenProvider.blacklistToken(accessToken);

                if (refreshToken != null) {
                    tokenProvider.blacklistToken(refreshToken);
                }

                if (logoutRequest != null && logoutRequest.isLogoutFromAllDevices()) {
                    tokenProvider.blacklistAllUserTokens(userId);
                }
            }

            clearTokenCookies(response);

            return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            clearTokenCookies(response);
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

//    @GetMapping("/me")
//    @Operation(summary = "Get current user", description = "Get current user info from cookie")
//    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
//        try {
//            String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
//
//            if (accessToken == null || !tokenProvider.validateToken(accessToken)) {
//                return ResponseEntity
//                        .status(HttpStatus.UNAUTHORIZED)
//                        .body(new ApiResponse(false, "No valid authentication found"));
//            }
//
//            Long userId = tokenProvider.getUserIdFromToken(accessToken);
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//            AuthResponse authResponse = new AuthResponse();
//            authResponse.setUserId(user.getId());
//            authResponse.setEmail(user.getEmail());
//            authResponse.setName(user.getName());
//            authResponse.setImageUrl(user.getImageUrl());
//
//            return ResponseEntity.ok(authResponse);
//        } catch (Exception e) {
//            return ResponseEntity
//                    .status(HttpStatus.UNAUTHORIZED)
//                    .body(new ApiResponse(false, "Invalid authentication"));
//        }
//    }

//    @PostMapping("/oauth2/google")
//    @Operation(summary = "Google OAuth2 login", description = "Processes Google OAuth2 token and sets JWT cookies")
//    public ResponseEntity<?> googleLogin(
//            @Valid @RequestBody GoogleTokenRequest tokenRequest,
//            HttpServletResponse response) {
//        try {
//            HttpTransport transport = new NetHttpTransport();
//            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                    .setAudience(Collections.singletonList(googleClientId))
//                    .build();
//
//            GoogleIdToken idToken = verifier.verify(tokenRequest.getTokenId());
//            if (idToken == null) {
//                return ResponseEntity
//                        .badRequest()
//                        .body(new ApiResponse(false, "Invalid Google token"));
//            }
//
//            GoogleIdToken.Payload payload = idToken.getPayload();
//            String email = payload.getEmail();
//            String name = (String) payload.get("name");
//            String googleId = payload.getSubject();
//            String pictureUrl = (String) payload.get("picture");
//
//            User user = userRepository.findByEmail(email).orElse(null);
//
//            if (user == null) {
//                user = new User();
//                user.setEmail(email);
//                user.setName(name);
//                user.setImageUrl(pictureUrl);
//                user.setProvider(AuthProvider.GOOGLE);
//                user.setProviderId(googleId);
//                user.setEmailVerified(true);
//                user = userRepository.save(user);
//            } else if (user.getProvider() != AuthProvider.GOOGLE) {
//                return ResponseEntity
//                        .badRequest()
//                        .body(new ApiResponse(false, "You have previously signed up with " + user.getProvider() +
//                                ". Please use that method to login."));
//            }
//
//            Map<String, Object> tokens = tokenProvider.createTokens(user);
//
//            setTokenCookies(response,
//                    (String) tokens.get("accessToken"),
//                    (String) tokens.get("refreshToken"));
//
//            AuthResponse authResponse = new AuthResponse();
//            authResponse.setUserId(user.getId());
//            authResponse.setEmail(user.getEmail());
//            authResponse.setName(user.getName());
//            authResponse.setImageUrl(user.getImageUrl());
//            authResponse.setTokenType("Bearer");
//            authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
//            authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));
//
//            return ResponseEntity.ok(authResponse);
//        } catch (Exception e) {
//            log.error("Google authentication error", e);
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, "Failed to process Google authentication: " + e.getMessage()));
//        }
//    }


    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and sets JWT tokens as HTTP-only cookies")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

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

        Map<String, Object> tokens = tokenProvider.createTokens(user);

        setTokenCookies(response,
                (String) tokens.get("accessToken"),
                (String) tokens.get("refreshToken"));

        // Use encrypted response
        EncryptedAuthResponse authResponse = new EncryptedAuthResponse();
        authResponse.setUserId(user.getId());
        authResponse.setEmail(user.getEmail());
        authResponse.setName(user.getName());
        authResponse.setImageUrl(user.getImageUrl());
        authResponse.setTokenType("Bearer");
        authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
        authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));

        return ResponseEntity.ok(authResponse);
    }

    // Replace the refresh method return with this:
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token from cookies")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE);

            if (refreshToken == null) {
                clearTokenCookies(response);
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "No refresh token found. Please login again."));
            }

            if (!tokenProvider.validateRefreshToken(refreshToken)) {
                clearTokenCookies(response);
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Invalid or expired refresh token. Please login again."));
            }

            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            tokenProvider.blacklistToken(refreshToken);

            Map<String, Object> tokens = tokenProvider.createTokens(user);

            setTokenCookies(response,
                    (String) tokens.get("accessToken"),
                    (String) tokens.get("refreshToken"));

            // Use encrypted response
            EncryptedAuthResponse authResponse = new EncryptedAuthResponse();
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setName(user.getName());
            authResponse.setImageUrl(user.getImageUrl());
            authResponse.setTokenType("Bearer");
            authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
            authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            clearTokenCookies(response);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid refresh token. Please login again."));
        }
    }

    // Replace the getCurrentUser method return with this:
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current user info from cookie")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);

            if (accessToken == null || !tokenProvider.validateToken(accessToken)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "No valid authentication found"));
            }

            Long userId = tokenProvider.getUserIdFromToken(accessToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Use encrypted response
            EncryptedAuthResponse authResponse = new EncryptedAuthResponse();
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setName(user.getName());
            authResponse.setImageUrl(user.getImageUrl());

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid authentication"));
        }
    }

    // Replace the googleLogin method return with this:
    @PostMapping("/oauth2/google")
    @Operation(summary = "Google OAuth2 login", description = "Processes Google OAuth2 token and sets JWT cookies")
    public ResponseEntity<?> googleLogin(
            @Valid @RequestBody GoogleTokenRequest tokenRequest,
            HttpServletResponse response) {
        try {
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenRequest.getTokenId());
            if (idToken == null) {
                return ResponseEntity
                        .badRequest()
                        .body(new ApiResponse(false, "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();
            String pictureUrl = (String) payload.get("picture");

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
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

            Map<String, Object> tokens = tokenProvider.createTokens(user);

            setTokenCookies(response,
                    (String) tokens.get("accessToken"),
                    (String) tokens.get("refreshToken"));

            // Use encrypted response
            EncryptedAuthResponse authResponse = new EncryptedAuthResponse();
            authResponse.setUserId(user.getId());
            authResponse.setEmail(user.getEmail());
            authResponse.setName(user.getName());
            authResponse.setImageUrl(user.getImageUrl());
            authResponse.setTokenType("Bearer");
            authResponse.setAccessTokenExpiresIn((Long) tokens.get("accessTokenExpiresIn"));
            authResponse.setRefreshTokenExpiresIn((Long) tokens.get("refreshTokenExpiresIn"));

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Google authentication error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to process Google authentication: " + e.getMessage()));
        }
    }

    // Cookie Management Helper Methods
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (accessTokenExpirationMsec / 1000));
        accessTokenCookie.setAttribute("SameSite", "Lax");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpirationMsec / 1000));
        refreshTokenCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshTokenCookie);

        log.info("Set cookies - Access token expires in {} seconds, Refresh token expires in {} seconds",
                accessTokenExpirationMsec / 1000, refreshTokenExpirationMsec / 1000);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        log.info("Cleared authentication cookies");
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