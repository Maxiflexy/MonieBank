package com.maxiflexy.auth_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2UserService oAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REMOVE CORS configuration - let API Gateway handle it
                // .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF (using SameSite cookies for protection)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                // Public endpoints
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/user/{userId}",
                                "/api/auth/oauth2/**",
                                "/api/auth/refresh",
                                "/api/auth/validate-token",
                                // Documentation endpoints
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )

                // Configure OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.baseUri("/api/auth/oauth2/authorize"))
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/api/auth/oauth2/callback/*"))
                        .userInfoEndpoint(endpoint ->
                                endpoint.userService(oAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        return http.build();
    }

    // REMOVE the corsConfigurationSource bean entirely

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}