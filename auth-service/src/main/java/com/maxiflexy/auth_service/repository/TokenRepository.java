package com.maxiflexy.auth_service.repository;

import com.maxiflexy.auth_service.enums.TokenType;
import com.maxiflexy.auth_service.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValueAndIsBlacklistedFalse(String tokenValue);

    Optional<Token> findByTokenValue(String tokenValue);

    List<Token> findByUserIdAndTokenTypeAndIsBlacklistedFalse(Long userId, TokenType tokenType);

    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.isBlacklisted = true, t.updatedAt = :now WHERE t.tokenValue = :tokenValue")
    void blacklistToken(@Param("tokenValue") String tokenValue, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE Token t SET t.isBlacklisted = true, t.updatedAt = :now WHERE t.userId = :userId AND t.isBlacklisted = false")
    void blacklistAllUserTokens(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    boolean existsByTokenValueAndIsBlacklistedTrue(String tokenValue);
}