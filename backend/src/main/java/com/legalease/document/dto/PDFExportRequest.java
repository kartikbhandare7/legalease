package com.legalease.document.dto;

import com.legalease.common.enums.PDFDocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PDFExportRequest {
    @NotNull(message = "Document type is required")
    private PDFDocumentType documentType;

    @NotNull(message = "Document ID is required")
    private UUID documentId;
}
