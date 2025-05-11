package com.maxiflexy.auth_service.security;

import com.maxiflexy.auth_service.enums.AuthProvider;
import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Get the Provider (Google)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        // Extract information from OAuth2 User
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String imageUrl = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getAttribute("sub");

        if(!StringUtils.hasText(email)) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if(userOptional.isPresent()) {
            user = userOptional.get();

            // Update existing user information if provider is the same
            if(!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new RuntimeException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }

            user = updateExistingUser(user, name, imageUrl);
        } else {
            // Register new user
            user = registerNewUser(registrationId, providerId, name, email, imageUrl);
        }

        return oAuth2User;
    }

    private User registerNewUser(String registrationId, String providerId, String name, String email, String imageUrl) {
        User user = new User();

        user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        user.setProviderId(providerId);
        user.setName(name);
        user.setEmail(email);
        user.setImageUrl(imageUrl);
        user.setEmailVerified(true); // Set to true since Google verifies emails

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, String name, String imageUrl) {
        existingUser.setName(name);
        existingUser.setImageUrl(imageUrl);
        return userRepository.save(existingUser);
    }
}