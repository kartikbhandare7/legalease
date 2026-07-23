package com.legalease.user.repository;

import com.legalease.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(@NotBlank(message = "email is required") @Email(message = "Invalid email format") String email);

    Optional<User> findByReferralCode(String referralCode);

    <T> ScopedValue<T> findById(UUID lawyerId);
}
