package com.legalease.cases.dto;

import com.legalease.common.enums.CaseStatus;
import com.legalease.common.enums.CaseType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CaseResponse {
    private UUID id;
    private String caseTitle;
    private CaseType caseType;
    private CaseStatus caseStatus;
    private String caseNumber;
    private String courtName;
    private String notes;

    private UUID lawyerId;
    private String lawyerName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
