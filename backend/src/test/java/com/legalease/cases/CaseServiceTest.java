package com.legalease.cases;

import com.legalease.cases.dto.CaseRequest;
import com.legalease.cases.dto.CaseStatusUpdateRequest;
import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.cases.service.CaseService;
import com.legalease.common.enums.CaseStatus;
import com.legalease.common.enums.CaseType;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaseService Tests")
class CaseServiceTest {

    @Mock private CaseRepository caseRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CaseService caseService;

    private UUID lawyerId;
    private UUID caseId;
    private User lawyer;
    private Case activeCase;
    private Case closedCase;
    private CaseRequest validRequest;

    @BeforeEach
    void setUp() {
        lawyerId = UUID.randomUUID();
        caseId   = UUID.randomUUID();

        lawyer = User.builder()
                .id(lawyerId)
                .fullName("Adv. Rahul Sharma")
                .email("rahul@legalease.com")
                .role(UserRole.ROLE_LAWYER)
                .build();

        activeCase = Case.builder()
                .id(caseId)
                .lawyer(lawyer)
                .caseTitle("Sharma vs State")
                .caseType(CaseType.CRIMINAL)
                .caseStatus(CaseStatus.ACTIVE)
                .courtName("Mumbai High Court")
                .caseNumber("CRI/001/2024")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        closedCase = Case.builder()
                .id(UUID.randomUUID())
                .lawyer(lawyer)
                .caseTitle("Old Property Case")
                .caseType(CaseType.PROPERTY)
                .caseStatus(CaseStatus.CLOSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new CaseRequest();
        validRequest.setCaseTitle("Sharma vs State");
        validRequest.setCaseType(CaseType.CRIMINAL);
        validRequest.setCourtName("Mumbai High Court");
        validRequest.setCaseNumber("CRI/001/2024");
    }

    // ── CREATE TESTS ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Create case")
    class CreateCaseTests {

        @Test
        @DisplayName("Should create case successfully with ACTIVE status")
        void shouldCreateCaseWithActiveStatus() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.save(any(Case.class)))
                    .thenReturn(activeCase);

            var response = caseService.createCase(validRequest, lawyerId);

            assertThat(response.getCaseTitle()).isEqualTo("Sharma vs State");
            assertThat(response.getCaseStatus()).isEqualTo(CaseStatus.ACTIVE);
            assertThat(response.getLawyerId()).isEqualTo(lawyerId);

            verify(caseRepository).save(any(Case.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when lawyer not found")
        void shouldThrowWhenLawyerNotFound() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    caseService.createCase(validRequest, lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Lawyer not found");

            verify(caseRepository, never()).save(any());
        }
    }

    // ── READ TESTS ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Get cases")
    class GetCaseTests {

        @Test
        @DisplayName("Should return paginated cases for lawyer")
        void shouldReturnPaginatedCasesForLawyer() {
            Page<Case> casePage = new PageImpl<>(List.of(activeCase));
            when(caseRepository.findByLawyerIdOrderByCreatedAtDesc(
                    eq(lawyerId), any(Pageable.class)))
                    .thenReturn(casePage);

            var result = caseService.getCases(lawyerId, null, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCaseTitle())
                    .isEqualTo("Sharma vs State");
        }

        @Test
        @DisplayName("Should filter by ACTIVE status when status param provided")
        void shouldFilterByStatus() {
            Page<Case> casePage = new PageImpl<>(List.of(activeCase));
            when(caseRepository.findByLawyerIdAndCaseStatusOrderByCreatedAtDesc(
                    eq(lawyerId), eq(CaseStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(casePage);

            var result = caseService.getCases(
                    lawyerId, CaseStatus.ACTIVE, null, 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(caseRepository)
                    .findByLawyerIdAndCaseStatusOrderByCreatedAtDesc(
                            eq(lawyerId), eq(CaseStatus.ACTIVE), any());
        }

        @Test
        @DisplayName("Should use keyword search when keyword provided")
        void shouldSearchByKeyword() {
            Page<Case> casePage = new PageImpl<>(List.of(activeCase));
            when(caseRepository.searchByTitle(
                    eq(lawyerId), eq("sharma"), any(Pageable.class)))
                    .thenReturn(casePage);

            var result = caseService.getCases(
                    lawyerId, null, "sharma", 0, 10);

            assertThat(result.getContent()).hasSize(1);
            verify(caseRepository).searchByTitle(
                    eq(lawyerId), eq("sharma"), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown caseId")
        void shouldThrowForUnknownCaseId() {
            when(caseRepository.findByIdAndLawyerId(any(), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    caseService.getCaseById(UUID.randomUUID(), lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Case not found or access denied");
        }
    }

    // ── STATUS UPDATE TESTS ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Update case status")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should update ACTIVE case to ON_HOLD successfully")
        void shouldUpdateActiveToOnHold() {
            var request = new CaseStatusUpdateRequest();
            request.setCaseStatus(CaseStatus.ON_HOLD);

            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(activeCase));
            when(caseRepository.save(any(Case.class)))
                    .thenReturn(activeCase);

            var response = caseService.updateCaseStatus(
                    caseId, request, lawyerId);

            verify(caseRepository).save(any(Case.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when reopening a CLOSED case")
        void shouldThrowWhenReopeningClosedCase() {
            var request = new CaseStatusUpdateRequest();
            request.setCaseStatus(CaseStatus.ACTIVE);

            when(caseRepository.findByIdAndLawyerId(
                    closedCase.getId(), lawyerId))
                    .thenReturn(Optional.of(closedCase));

            assertThatThrownBy(() ->
                    caseService.updateCaseStatus(
                            closedCase.getId(), request, lawyerId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Closed cases cannot be reopened");
        }

        @Test
        @DisplayName("Should close an ACTIVE case successfully")
        void shouldCloseActiveCase() {
            var request = new CaseStatusUpdateRequest();
            request.setCaseStatus(CaseStatus.CLOSED);

            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(activeCase));
            when(caseRepository.save(any(Case.class)))
                    .thenReturn(activeCase);

            caseService.updateCaseStatus(caseId, request, lawyerId);

            verify(caseRepository).save(
                    argThat(c -> c.getCaseStatus() == CaseStatus.CLOSED));
        }
    }

    // ── DELETE TESTS ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Delete case")
    class DeleteCaseTests {

        @Test
        @DisplayName("Should delete case when lawyer owns it")
        void shouldDeleteOwnedCase() {
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(activeCase));

            caseService.deleteCase(caseId, lawyerId);

            verify(caseRepository).delete(activeCase);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when case not owned")
        void shouldThrowWhenCaseNotOwned() {
            UUID anotherLawyerId = UUID.randomUUID();
            when(caseRepository.findByIdAndLawyerId(caseId, anotherLawyerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    caseService.deleteCase(caseId, anotherLawyerId))
                    .isInstanceOf(ResourceNotFoundException.class);

            // Must never delete if not owned
            verify(caseRepository, never()).delete(any());
        }
    }

    // ── DASHBOARD COUNT TESTS ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Dashboard counts")
    class DashboardCountTests {

        @Test
        @DisplayName("Should return correct active case count")
        void shouldReturnActiveCaseCount() {
            when(caseRepository.countByLawyerIdAndCaseStatus(
                    lawyerId, CaseStatus.ACTIVE))
                    .thenReturn(5L);

            long count = caseService.getActiveCaseCount(lawyerId);

            assertThat(count).isEqualTo(5L);
        }
    }
}