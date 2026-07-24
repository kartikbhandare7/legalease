package com.legalease.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ClientIntakeRequest {

    @NotNull(message = "Case ID is required")
    private UUID caseId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    private String phone;
    private String email;
    private String opposingParty;

    private String caseBackground;
    private String rawIntakeNote;
    private boolean aiAssisted;
}
