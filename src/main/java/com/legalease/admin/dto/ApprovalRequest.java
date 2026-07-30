package com.legalease.admin.dto;

import com.legalease.common.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;
    private String rejectionReason;
}
