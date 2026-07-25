package com.legalease.hearing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class HearingLogRequest {
    @NotNull(message = "Case ID is required")
    private UUID caseId;

    @NotNull(message = "Hearing date is required")
    private LocalDate hearingDate;

    private String outcome;
    private LocalDate nextDate;

    private String actionItems;
    private String rawNote;

    private boolean aiAssisted;
}
