package com.legalease.hearing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HearingUpdateRequest {
    @NotNull(message = "Hearing data is Required")
    private LocalDate hearingDate;

    private String outcome;
    private LocalDate nextDate;
    private String actionItems;
}
