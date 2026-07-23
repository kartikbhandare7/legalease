package com.legalease.cases.service;

import com.legalease.cases.dto.CaseRequest;
import com.legalease.cases.dto.CaseResponse;
import com.legalease.cases.dto.CaseStatusUpdateRequest;
import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.common.enums.CaseStatus;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public CaseResponse createCase(CaseRequest request, UUID lawyerId) {

        User lawyer = (User) userRepository.findById(lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lawyer not found"));

        Case newCase = Case.builder()
                .lawyer(lawyer)
                .caseTitle(request.getCaseTitle())
                .caseType(request.getCaseType())
                .caseStatus(CaseStatus.ACTIVE)   // always starts ACTIVE
                .caseNumber(request.getCaseNumber())
                .courtName(request.getCourtName())
                .notes(request.getNotes())
                .build();

        Case saved = caseRepository.save(newCase);
        return mapToResponse(saved);
    }

    // ── READ ALL (paginated) ──────────────────────────────────────────────────

    public Page<CaseResponse> getCases(UUID lawyerId,
                                       CaseStatus status,
                                       String keyword,
                                       int page,
                                       int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Keyword search takes priority
        if (keyword != null && !keyword.isBlank()) {
            return caseRepository
                    .searchByTitle(lawyerId, keyword.trim(), pageable)
                    .map(this::mapToResponse);
        }

        // Filter by status if provided
        if (status != null) {
            return caseRepository
                    .findByLawyerIdAndCaseStatusOrderByCreatedAtDesc(
                            lawyerId, status, pageable)
                    .map(this::mapToResponse);
        }

        // All cases — default dashboard view
        return caseRepository
                .findByLawyerIdOrderByCreatedAtDesc(lawyerId, pageable)
                .map(this::mapToResponse);
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────

    public CaseResponse getCaseById(UUID caseId, UUID lawyerId) {
        Case found = findOwnedCase(caseId, lawyerId);
        return mapToResponse(found);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public CaseResponse updateCase(UUID caseId,
                                   CaseRequest request,
                                   UUID lawyerId) {
        Case existing = findOwnedCase(caseId, lawyerId);

        existing.setCaseTitle(request.getCaseTitle());
        existing.setCaseType(request.getCaseType());
        existing.setCaseNumber(request.getCaseNumber());
        existing.setCourtName(request.getCourtName());
        existing.setNotes(request.getNotes());

        return mapToResponse(caseRepository.save(existing));
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────────────

    @Transactional
    public CaseResponse updateCaseStatus(UUID caseId,
                                         CaseStatusUpdateRequest request,
                                         UUID lawyerId) {
        Case existing = findOwnedCase(caseId, lawyerId);

        // Cannot reopen a CLOSED case — needs a new filing
        if (existing.getCaseStatus() == CaseStatus.CLOSED &&
                request.getCaseStatus() == CaseStatus.ACTIVE) {
            throw new BadRequestException(
                    "Closed cases cannot be reopened. Please file a new case.");
        }

        existing.setCaseStatus(request.getCaseStatus());
        return mapToResponse(caseRepository.save(existing));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteCase(UUID caseId, UUID lawyerId) {
        Case existing = findOwnedCase(caseId, lawyerId);
        caseRepository.delete(existing);
    }

    // ── DASHBOARD COUNTS ──────────────────────────────────────────────────────

    public long getActiveCaseCount(UUID lawyerId) {
        return caseRepository.countByLawyerIdAndCaseStatus(
                lawyerId, CaseStatus.ACTIVE);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    // Central ownership check — never let lawyer A touch lawyer B's case
    private Case findOwnedCase(UUID caseId, UUID lawyerId) {
        return caseRepository.findByIdAndLawyerId(caseId, lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Case not found or access denied"));
    }

    private CaseResponse mapToResponse(Case c) {
        return CaseResponse.builder()
                .id(c.getId())
                .caseTitle(c.getCaseTitle())
                .caseType(c.getCaseType())
                .caseStatus(c.getCaseStatus())
                .caseNumber(c.getCaseNumber())
                .courtName(c.getCourtName())
                .notes(c.getNotes())
                .lawyerId(c.getLawyer().getId())
                .lawyerName(c.getLawyer().getFullName())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}