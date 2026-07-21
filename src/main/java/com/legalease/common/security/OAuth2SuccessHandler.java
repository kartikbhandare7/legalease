package com.legalease.common.security;

import com.legalease.common.enums.AccountStatus;
import com.legalease.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email =  oAuth2User.getAttribute("email");

        var user = userRepository.findByEmail(email)
                .orElseThrow();

        if(user.getAccountStatus() == AccountStatus.PENDING){
            response.sendRedirect(frontendUrl + "/pending-approval");
            return;
        }
        String token = jwtService.generateToken(email,user.getRole().name(),user.getId());

        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + token);
    }
}
