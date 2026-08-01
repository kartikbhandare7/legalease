package com.legalease.hearing.service;

import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.hearing.dto.HearingLogRequest;
import com.legalease.hearing.dto.HearingResponse;
import com.legalease.hearing.dto.HearingUpdateRequest;
import com.legalease.hearing.model.Hearing;
import com.legalease.hearing.repository.HearingRepository;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HearingService {

    private final HearingRepository hearingRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public HearingResponse logHearing(HearingLogRequest request,
                                      UUID lawyerId) {
        User lawyer = userRepository.findById(lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lawyer not found"));

        // Verify case ownership before linking hearing to it
        Case legalCase = caseRepository
                .findByIdAndLawyerId(request.getCaseId(), lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Case not found or access denied"));

        // Prevent duplicate hearing log for same case and date
        if (hearingRepository.existsByLegalCaseIdAndHearingDate(
                request.getCaseId(), request.getHearingDate())) {
            throw new BadRequestException(
                    "A hearing is already logged for this case on "
                            + request.getHearingDate());
        }

        Hearing hearing = Hearing.builder()
                .legalCase(legalCase)
                .lawyer(lawyer)
                .hearingDate(request.getHearingDate())
                .outcome(request.getOutcome())
                .nextDate(request.getNextDate())
                .actionItems(request.getActionItems())
                .rawNote(request.getRawNote())
                .aiAssisted(request.isAiAssisted())
                .build();

        return mapToResponse(hearingRepository.save(hearing));
    }

    // ── READ — hearings for a case ────────────────────────────────────────────

    public Page<HearingResponse> getHearingsByCase(UUID caseId,
                                                   UUID lawyerId,
                                                   int page,
                                                   int size) {
        // Verify case ownership first
        caseRepository.findByIdAndLawyerId(caseId, lawyerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Case not found or access denied"));

        Pageable pageable = PageRequest.of(page, size);
        return hearingRepository
                .findByLegalCaseIdOrderByHearingDateDesc(caseId, pageable)
                .map(this::mapToResponse);
    }

    // ── READ — all hearings for lawyer ────────────────────────────────────────

    public Page<HearingResponse> getAllHearings(UUID lawyerId,
                                                int page,
                                                int size) {
        Pageable pageable = PageRequest.of(page, size);
        return hearingRepository
                .findByLawyerIdOrderByHearingDateDesc(lawyerId, pageable)
                .map(this::mapToResponse);
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────

    public HearingResponse getHearingById(UUID hearingId, UUID lawyerId) {
        return mapToResponse(findOwnedHearing(hearingId, lawyerId));
    }

    // ── READ — upcoming hearings for dashboard ────────────────────────────────

    public List<HearingResponse> getUpcomingHearings(UUID lawyerId) {
        return hearingRepository
                .findUpcomingHearings(lawyerId, LocalDate.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public HearingResponse updateHearing(UUID hearingId,
                                         HearingUpdateRequest request,
                                         UUID lawyerId) {
        Hearing existing = findOwnedHearing(hearingId, lawyerId);

        existing.setHearingDate(request.getHearingDate());
        existing.setOutcome(request.getOutcome());
        existing.setNextDate(request.getNextDate());
        existing.setActionItems(request.getActionItems());

        // aiAssisted flag never changes on update — audit integrity
        return mapToResponse(hearingRepository.save(existing));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteHearing(UUID hearingId, UUID lawyerId) {
        Hearing existing = findOwnedHearing(hearingId, lawyerId);
        hearingRepository.delete(existing);
    }

    // ── STATS ─────────────────────────────────────────────────────────────────

    public long getAiAssistedHearingCount(UUID lawyerId) {
        return hearingRepository
                .countByLawyerIdAndAiAssistedTrue(lawyerId);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────

    private Hearing findOwnedHearing(UUID hearingId, UUID lawyerId) {
        return hearingRepository.findByIdAndLawyerId(hearingId, lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hearing not found or access denied"));
    }

    private HearingResponse mapToResponse(Hearing h) {
        return HearingResponse.builder()
                .id(h.getId())
                .caseId(h.getLegalCase().getId())
                .caseTitle(h.getLegalCase().getCaseTitle())
                .lawyerId(h.getLawyer().getId())
                .lawyerName(h.getLawyer().getFullName())
                .hearingDate(h.getHearingDate())
                .outcome(h.getOutcome())
                .nextDate(h.getNextDate())
                .actionItems(h.getActionItems())
                .rawNote(h.getRawNote())
                .aiAssisted(h.isAiAssisted())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}