package com.legalease.cases.dto;

import com.legalease.common.enums.CaseStatus;
import jakarta.validation.constraints.NotNull;

public class CaseStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private CaseStatus status;
}
