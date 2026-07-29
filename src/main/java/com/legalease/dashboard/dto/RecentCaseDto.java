package com.legalease.dashboard.dto;

import com.legalease.common.enums.CaseStatus;
import com.legalease.common.enums.CaseType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RecentCaseDto {

    private UUID id;
    private String caseTitle;
    private CaseType caseType;
    private CaseStatus caseStatus;
    private String courtName;
    private LocalDateTime createdAt;
}
