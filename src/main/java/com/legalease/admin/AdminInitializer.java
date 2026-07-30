package com.legalease.admin;

import com.legalease.common.enums.UserRole;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail("admin@legalease.com").isEmpty()) {

            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@legalease.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            System.out.println("Default admin created.");
        }
    }
}
