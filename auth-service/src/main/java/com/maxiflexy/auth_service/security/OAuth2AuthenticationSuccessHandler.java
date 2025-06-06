package com.maxiflexy.auth_service.security;

import com.maxiflexy.auth_service.model.User;
import com.maxiflexy.auth_service.repository.UserRepository;
import com.maxiflexy.auth_service.service.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.auth.accessTokenExpirationMsec}")
    private long accessTokenExpirationMsec;

    @Value("${app.auth.refreshTokenExpirationMsec}")
    private long refreshTokenExpirationMsec;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // Cookie names (must match with AuthController)
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private String[] authorizedRedirectUris = new String[]{
            "http://localhost:5173/oauth2/redirect",
            "http://localhost:80/oauth2/redirect",
            "http://localhost:3000/oauth2/redirect"
    };

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String redirectUri = request.getParameter("redirect_uri");
        if (redirectUri != null && !isAuthorizedRedirectUri(redirectUri)) {
            throw new IllegalArgumentException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri != null ? redirectUri : getFrontendRedirectUrl();

        org.springframework.security.oauth2.core.user.OAuth2User oAuth2User =
                (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new RuntimeException("User not found with email: " + email));

        // Create tokens using the new method
        Map<String, Object> tokens = tokenProvider.createTokens(user);

        // Set HTTP-only cookies
        setTokenCookies(response,
                (String) tokens.get("accessToken"),
                (String) tokens.get("refreshToken"));

        // Build redirect URL without tokens (they're in cookies now)
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("userId", user.getId())
                .queryParam("email", user.getEmail())
                .queryParam("name", user.getName())
                .queryParam("success", "true")
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        for (String authorizedRedirectUri : authorizedRedirectUris) {
            if (uri.startsWith(authorizedRedirectUri)) {
                return true;
            }
        }
        return false;
    }

    // Changed method name to avoid conflict with parent class method
    private String getFrontendRedirectUrl() {
        return frontendBaseUrl + "oauth2/redirect";
    }

    // Cookie Management Helper Methods (same as in AuthController)
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

        // Fixed logger call - convert long to String for proper formatting
        logger.info("Set OAuth2 cookies - Access token expires in {} seconds, Refresh token expires in {} seconds",
                String.valueOf(accessTokenExpirationMsec / 1000),
                String.valueOf(refreshTokenExpirationMsec / 1000));
    }
}