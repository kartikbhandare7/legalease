package com.legalease.client.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientUpdateRequest {

    @NotBlank(message = "Client name is required")
    private String clientName;

    private String phone;
    private String email;
    private String opposingParty;
    private String caseBackground;
}
