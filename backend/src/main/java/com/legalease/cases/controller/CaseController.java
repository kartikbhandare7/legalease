package com.legalease.cases.controller;

import com.legalease.cases.dto.CaseRequest;
import com.legalease.cases.dto.CaseResponse;
import com.legalease.cases.dto.CaseStatusUpdateRequest;
import com.legalease.cases.service.CaseService;
import com.legalease.common.enums.CaseStatus;
import com.legalease.common.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    // Only lawyers create cases — clerks are read+log only
    @PostMapping
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CaseRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(caseService.createCase(request, lawyerId));
    }

    // Both LAWYER and CLERK can read cases
    @GetMapping
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<Page<CaseResponse>> getCases(
            @CurrentUser UUID lawyerId,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                caseService.getCases(lawyerId, status, keyword, page, size));
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<CaseResponse> getCaseById(
            @PathVariable UUID caseId,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(caseService.getCaseById(caseId, lawyerId));
    }

    @PutMapping("/{caseId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<CaseResponse> updateCase(
            @PathVariable UUID caseId,
            @Valid @RequestBody CaseRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                caseService.updateCase(caseId, request, lawyerId));
    }

    // Separate endpoint for status change — single responsibility
    @PatchMapping("/{caseId}/status")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<CaseResponse> updateStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody CaseStatusUpdateRequest request,
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(
                caseService.updateCaseStatus(caseId, request, lawyerId));
    }

    @DeleteMapping("/{caseId}")
    @PreAuthorize("hasRole('LAWYER')")
    public ResponseEntity<Void> deleteCase(
            @PathVariable UUID caseId,
            @CurrentUser UUID lawyerId) {
        caseService.deleteCase(caseId, lawyerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('LAWYER', 'CLERK')")
    public ResponseEntity<Long> getActiveCaseCount(
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(caseService.getActiveCaseCount(lawyerId));
    }
}