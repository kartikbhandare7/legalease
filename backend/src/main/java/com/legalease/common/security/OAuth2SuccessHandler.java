package com.legalease.common.security;

import com.legalease.common.enums.AccountStatus;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Injected from application.yml — where to redirect after Google login
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Fetch the full user — was created in OAuth2UserService on first login
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found after OAuth2 login: " + email));

        log.info("OAuth2 login success for: {} | status: {}",
                email, user.getAccountStatus());

        // PENDING lawyers — cannot access the app yet
        // Redirect to waiting page with no token issued
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            log.info("User {} is PENDING — redirecting to pending-approval", email);
            response.sendRedirect(frontendUrl + "/pending-approval");
            return;
        }

        // REJECTED accounts — block entirely
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            log.warn("Rejected user {} attempted OAuth2 login", email);
            response.sendRedirect(frontendUrl + "/login?error=rejected");
            return;
        }

        // ACTIVE users — issue JWT and redirect to frontend callback
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        log.info("JWT issued for OAuth2 user: {}", email);

        // Frontend OAuth2Callback.jsx reads the token from this URL param
        // stores it in Redux + localStorage and redirects to dashboard
        response.sendRedirect(
                frontendUrl + "/oauth2/callback?token=" + token);
    }
}