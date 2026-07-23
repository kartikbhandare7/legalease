package com.legalease.cases.dto;

import com.legalease.common.enums.CaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CaseRequest {
    @NotBlank(message = "Case title is required")
    private String caseTitle;

    @NotNull(message = "Case type is required")
    private CaseType caseType;

    private String caseNumber;

    private String courtName;

    private String notes;
}
