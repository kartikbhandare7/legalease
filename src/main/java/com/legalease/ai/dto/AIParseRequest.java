package com.legalease.ai.dto;

import com.legalease.common.enums.AIParseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AIParseRequest {
    @NotBlank(message = "Raw text is required")
    private String rawText;

    @NotNull(message = "Parse type is required")
    private AIParseType parseType;
}
