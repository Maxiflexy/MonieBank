package com.maxiflexy.auth_service.service;

import com.maxiflexy.auth_service.enums.TokenType;
import com.maxiflexy.auth_service.model.Token;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

import static com.maxiflexy.auth_service.enums.TokenType.ACCESS_TOKEN;
import static com.maxiflexy.auth_service.enums.TokenType.REFRESH_TOKEN;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Value("${app.auth.tokenSecret}")
    private String tokenSecret;

    @Value("${app.auth.accessTokenExpirationMsec}")
    private long accessTokenExpirationMsec;

    @Value("${app.auth.refreshTokenExpirationMsec}")
    private long refreshTokenExpirationMsec;

    public Map<String, Object> createTokens(User user) {
        Map<String, Object> tokens = new HashMap<>();

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("accessTokenExpiresIn", accessTokenExpirationMsec);
        tokens.put("refreshTokenExpiresIn", refreshTokenExpirationMsec);

        return tokens;
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMsec);

        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject(Long.toString(user.getId()))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        // Save access token to database
        saveTokenToDatabase(token, ACCESS_TOKEN, user.getId(), expiryDate);

        return token;
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMsec);

        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .setSubject(Long.toString(user.getId()))
                .claim("email", user.getEmail())
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();

        // Save refresh token to database
        saveTokenToDatabase(token, REFRESH_TOKEN, user.getId(), expiryDate);

        return token;
    }

    private void saveTokenToDatabase(String tokenValue, TokenType tokenType, Long userId, Date expiryDate) {
        Token token = new Token();
        token.setTokenValue(tokenValue);
        token.setTokenType(tokenType);
        token.setUserId(userId);
        token.setExpiryDate(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        token.setIsBlacklisted(false);

        tokenRepository.save(token);
    }

    public Long getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public String getTokenType(String token) {
        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("tokenType", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(authToken)) {
                logger.error("Token is blacklisted");
                return false;
            }

            SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            // Check if refresh token is blacklisted
            if (isTokenBlacklisted(refreshToken)) {
                logger.error("Refresh token is blacklisted");
                return false;
            }

            SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            // Verify it's a refresh token
            String tokenType = claims.get("tokenType", String.class);
            if (!"REFRESH".equals(tokenType)) {
                logger.error("Token is not a refresh token");
                return false;
            }

            return true;
        } catch (Exception ex) {
            logger.error("Invalid refresh token: {}", ex.getMessage());
            return false;
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenRepository.existsByTokenValueAndIsBlacklistedTrue(token);
    }

    public void blacklistToken(String token) {
        tokenRepository.blacklistToken(token, LocalDateTime.now());
    }

    public void blacklistAllUserTokens(Long userId) {
        tokenRepository.blacklistAllUserTokens(userId, LocalDateTime.now());
    }

    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}