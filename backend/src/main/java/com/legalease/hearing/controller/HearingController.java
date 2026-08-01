package com.legalease.hearing.controller;

import com.legalease.common.security.CurrentUser;
import com.legalease.hearing.dto.HearingLogRequest;
import com.legalease.hearing.dto.HearingResponse;
import com.legalease.hearing.dto.HearingUpdateRequest;
import com.legalease.hearing.service.HearingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hearings")
@RequiredArgsConstructor
public class HearingController {

    private final HearingService hearingService;

    // Both LAWYER and CLERK can log hearings
    @PostMapping("/log")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<HearingResponse> logHearing(
            @Valid @RequestBody HearingLogRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                hearingService.logHearing(request, lawyerId));
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<Page<HearingResponse>> getHearingsByCase(
            @PathVariable UUID caseId,
            @CurrentUser UUID lawyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                hearingService.getHearingsByCase(caseId, lawyerId, page, size));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<Page<HearingResponse>> getAllHearings(
            @CurrentUser UUID lawyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                hearingService.getAllHearings(lawyerId, page, size));
    }

    // Upcoming hearings — powers the dashboard reminder widget
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<List<HearingResponse>> getUpcomingHearings(
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                hearingService.getUpcomingHearings(lawyerId));
    }

    @GetMapping("/{hearingId}")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<HearingResponse> getHearingById(
            @PathVariable UUID hearingId,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                hearingService.getHearingById(hearingId, lawyerId));
    }

    @PutMapping("/{hearingId}")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<HearingResponse> updateHearing(
            @PathVariable UUID hearingId,
            @Valid @RequestBody HearingUpdateRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                hearingService.updateHearing(hearingId, request, lawyerId));
    }

    // Only LAWYER can delete — clerk cannot remove hearing records
    @DeleteMapping("/{hearingId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Void> deleteHearing(
            @PathVariable UUID hearingId,
            @CurrentUser UUID lawyerId) {
        hearingService.deleteHearing(hearingId, lawyerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/ai-assisted")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Long> getAiAssistedCount(
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                hearingService.getAiAssistedHearingCount(lawyerId));
    }
}