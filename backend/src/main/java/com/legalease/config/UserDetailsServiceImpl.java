package com.legalease.config;

import com.legalease.common.enums.AccountStatus;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Look up user by email — our "username" is always email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email));

        // Block PENDING and REJECTED users at the authentication layer
        // This means even if someone has a valid JWT but gets rejected later,
        // they will be blocked on next request when Spring reloads UserDetails
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new UsernameNotFoundException(
                    "Account has been rejected: " + email);
        }

        // Spring Security needs GrantedAuthority — role enum maps directly
        // Password is nullable for Google OAuth users — use empty string
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}