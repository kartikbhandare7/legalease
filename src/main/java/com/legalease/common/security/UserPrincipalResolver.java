package com.legalease.common.security;

import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userPrincipalResolver")
@RequiredArgsConstructor
public class UserPrincipalResolver {

    private final UserRepository userRepository;

    // Called by @CurrentUser — resolves UserDetails email → UUID
    public UUID resolve(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow();
    }
}