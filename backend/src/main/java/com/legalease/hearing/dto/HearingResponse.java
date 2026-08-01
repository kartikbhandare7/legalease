package com.legalease.hearing.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HearingResponse {

    private UUID id;
    private UUID caseId;
    private String caseTitle;
    private UUID lawyerId;
    private String lawyerName;
    private LocalDate hearingDate;
    private String outcome;
    private LocalDate nextDate;
    private String actionItems;
    private String rawNote;
    private boolean aiAssisted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
