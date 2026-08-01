package com.legalease.auth.service;

import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.AuthProvider;
import com.legalease.common.enums.UserRole;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // Fetch user info from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        userRepository.findByEmail(email).ifPresentOrElse(
                existing ->{}, () -> {
                    User newUser = User.builder()
                            .fullName(name)
                            .email(email)
                            .authProvider(AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .role(UserRole.ROLE_LAWYER)
                            .accountStatus(AccountStatus.PENDING)
                            .referralCode(generateReferralCode())
                            .build();
                    userRepository.save(newUser);
                }
        );
        return oAuth2User;
    }

    private String generateReferralCode() {
        return "LAW-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
