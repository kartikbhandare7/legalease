package com.legalease.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIParseResponse {
    private String parsedFields;

    private String parseType;

    private long processingTimeMs;

    private boolean success;
    private String errorMessage;
}
