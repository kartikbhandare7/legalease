package com.legalease.user.dto;

import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.AuthProvider;
import com.legalease.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private UserRole role;
    private AuthProvider authProvider;
    private AccountStatus accountStatus;
    private String barCouncilNumber;

    private String certificatePath;

    private String referralCode;
    private LocalDateTime createdAt;
}
