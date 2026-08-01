package com.legalease.auth.dto;

import com.legalease.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private String barCouncilNumber;

    private String referralCode;
}
