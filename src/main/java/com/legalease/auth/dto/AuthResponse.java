package com.legalease.auth.dto;

import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String fullName;
    private UserRole role;
    private AccountStatus accountStatus;
    private UUID userId;

    private boolean approval;

}
