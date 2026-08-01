package com.legalease.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotNull(message = "email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "password is required")
    private String password;
}
