package com.legalease.ai.controller;

import com.legalease.ai.dto.AIParseRequest;
import com.legalease.ai.dto.AIParseResponse;
import com.legalease.ai.service.AIParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIParserService aiParserService;

    @PostMapping("/parse")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<AIParseResponse> parse(
            @Valid @RequestBody AIParseRequest request)  {
        return ResponseEntity.ok(aiParserService.parse(request));
    }
}
