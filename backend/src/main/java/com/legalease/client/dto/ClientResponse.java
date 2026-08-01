package com.legalease.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientResponse {

    private UUID id;
    private UUID caseId;
    private String caseTitle;
    private UUID lawyerId;
    private String lawyerName;
    private String clientName;
    private String phone;
    private String email;
    private String opposingParty;
    private String caseBackground;
    private String rawIntakeNote;
    private boolean aiAssisted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
