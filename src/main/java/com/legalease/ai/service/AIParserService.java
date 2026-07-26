package com.legalease.ai.service;

import com.legalease.ai.dto.AIParseRequest;
import com.legalease.ai.dto.AIParseResponse;
import com.legalease.common.enums.AIParseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIParserService {

    // Spring AI's fluent ChatClient — handles the LLM call
    private final ChatClient chatClient;

    public AIParseResponse parse(AIParseRequest request) {
        long start = System.currentTimeMillis();

        try {
            String prompt = buildPrompt(request);

            // Spring AI call — blocks until LLM responds
            String rawJson = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Strip markdown code fences if LLM adds them
            String cleaned = cleanJson(rawJson);

            long elapsed = System.currentTimeMillis() - start;
            log.info("AI parse completed in {}ms for type {}",
                    elapsed, request.getParseType());

            return AIParseResponse.builder()
                    .parsedFields(cleaned)
                    .parseType(request.getParseType().name())
                    .processingTimeMs(elapsed)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("AI parse failed for type {}: {}",
                    request.getParseType(), e.getMessage());

            return AIParseResponse.builder()
                    .parsedFields("{}")
                    .parseType(request.getParseType().name())
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .success(false)
                    .errorMessage("AI parsing failed. Please fill the form manually.")
                    .build();
        }
    }

    // ── PROMPT BUILDER ────────────────────────────────────────────────────────

    private String buildPrompt(AIParseRequest request) {
        return switch (request.getParseType()) {
            case INTAKE  -> buildIntakePrompt(request.getRawText());
            case HEARING -> buildHearingPrompt(request.getRawText());
        };
    }

    private String buildIntakePrompt(String rawText) {
        return """
            You are a legal assistant AI for Indian lawyers.
            Extract the following fields from the lawyer's raw note and return ONLY a valid JSON object.
            Do NOT include markdown, code fences, or explanation — only raw JSON.

            Fields to extract:
            - clientName (string) — full name of the client
            - opposingParty (string) — name of the opposing party
            - caseBackground (string) — summary of the case background
            - caseType (string) — one of: CIVIL, CRIMINAL, FAMILY, PROPERTY, CORPORATE, LABOUR, OTHER
            - courtName (string) — name of the court if mentioned
            - phone (string) — client phone number if mentioned
            - email (string) — client email if mentioned

            If a field is not found in the note, set it to null.

            Raw note from lawyer:
            "%s"

            Return only the JSON object. Example format:
            {"clientName":"John Doe","opposingParty":"Ravi Kumar","caseBackground":"...","caseType":"CRIMINAL","courtName":null,"phone":null,"email":null}
            """.formatted(rawText);
    }

    private String buildHearingPrompt(String rawText) {
        return """
            You are a legal assistant AI for Indian lawyers.
            Extract the following fields from the lawyer's raw hearing note and return ONLY a valid JSON object.
            Do NOT include markdown, code fences, or explanation — only raw JSON.

            Fields to extract:
            - hearingDate (string, format YYYY-MM-DD) — date of today's hearing if mentioned
            - nextDate (string, format YYYY-MM-DD) — next hearing date if mentioned
            - outcome (string) — what happened in court today
            - actionItems (array of strings) — list of tasks the lawyer needs to do before next hearing

            If a field is not found, set it to null. For actionItems return an empty array [] if none found.

            Today's date for reference: %s

            Raw note from lawyer:
            "%s"

            Return only the JSON object. Example format:
            {"hearingDate":"2024-08-01","nextDate":"2024-09-05","outcome":"Judge asked for evidence submission","actionItems":["Submit FIR copy","Notify client of next date"]}
            """.formatted(java.time.LocalDate.now(), rawText);
    }

    // Remove ```json ... ``` fences some LLMs add even when told not to
    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        return raw
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }
}