package com.legalease.hearing;

import com.legalease.cases.model.Case;
import com.legalease.cases.repository.CaseRepository;
import com.legalease.common.enums.CaseStatus;
import com.legalease.common.enums.CaseType;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.hearing.dto.HearingLogRequest;
import com.legalease.hearing.dto.HearingUpdateRequest;
import com.legalease.hearing.model.Hearing;
import com.legalease.hearing.repository.HearingRepository;
import com.legalease.hearing.service.HearingService;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HearingService Tests")
class HearingServiceTest {

    @Mock private HearingRepository hearingRepository;
    @Mock private CaseRepository caseRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private HearingService hearingService;

    private UUID lawyerId;
    private UUID caseId;
    private UUID hearingId;
    private User lawyer;
    private Case legalCase;
    private Hearing existingHearing;
    private HearingLogRequest validRequest;

    @BeforeEach
    void setUp() {
        lawyerId  = UUID.randomUUID();
        caseId    = UUID.randomUUID();
        hearingId = UUID.randomUUID();

        lawyer = User.builder()
                .id(lawyerId)
                .fullName("Adv. Rahul Sharma")
                .role(UserRole.ROLE_LAWYER)
                .build();

        legalCase = Case.builder()
                .id(caseId)
                .lawyer(lawyer)
                .caseTitle("Sharma vs State")
                .caseType(CaseType.CRIMINAL)
                .caseStatus(CaseStatus.ACTIVE)
                .build();

        existingHearing = Hearing.builder()
                .id(hearingId)
                .legalCase(legalCase)
                .lawyer(lawyer)
                .hearingDate(LocalDate.of(2024, 8, 1))
                .outcome("Judge asked for evidence")
                .nextDate(LocalDate.of(2024, 9, 5))
                .actionItems("[\"Submit FIR copy\"]")
                .rawNote("Sharma case hearing today judge asked evidence next date sep 5")
                .aiAssisted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new HearingLogRequest();
        validRequest.setCaseId(caseId);
        validRequest.setHearingDate(LocalDate.of(2024, 8, 1));
        validRequest.setOutcome("Judge asked for evidence");
        validRequest.setNextDate(LocalDate.of(2024, 9, 5));
        validRequest.setActionItems("[\"Submit FIR copy\"]");
        validRequest.setRawNote("raw note here");
        validRequest.setAiAssisted(true);
    }

    @Nested
    @DisplayName("Log hearing")
    class LogHearingTests {

        @Test
        @DisplayName("Should log hearing successfully with AI assisted flag")
        void shouldLogHearingSuccessfully() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(legalCase));
            when(hearingRepository.existsByLegalCaseIdAndHearingDate(
                    caseId, validRequest.getHearingDate()))
                    .thenReturn(false);
            when(hearingRepository.save(any(Hearing.class)))
                    .thenReturn(existingHearing);

            var response = hearingService.logHearing(validRequest, lawyerId);

            assertThat(response.getOutcome()).isEqualTo("Judge asked for evidence");
            assertThat(response.isAiAssisted()).isTrue();
            assertThat(response.getNextDate()).isEqualTo(LocalDate.of(2024, 9, 5));
            verify(hearingRepository).save(any(Hearing.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException on duplicate hearing date")
        void shouldThrowOnDuplicateHearingDate() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.of(legalCase));
            when(hearingRepository.existsByLegalCaseIdAndHearingDate(
                    caseId, validRequest.getHearingDate()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    hearingService.logHearing(validRequest, lawyerId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already logged");

            verify(hearingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when case not owned")
        void shouldThrowWhenCaseNotOwned() {
            when(userRepository.findById(lawyerId))
                    .thenReturn(Optional.of(lawyer));
            when(caseRepository.findByIdAndLawyerId(caseId, lawyerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    hearingService.logHearing(validRequest, lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Case not found or access denied");
        }
    }

    @Nested
    @DisplayName("Read hearings")
    class ReadHearingTests {

        @Test
        @DisplayName("Should return upcoming hearings sorted by next date")
        void shouldReturnUpcomingHearings() {
            when(hearingRepository.findUpcomingHearings(
                    eq(lawyerId), any(LocalDate.class)))
                    .thenReturn(List.of(existingHearing));

            var result = hearingService.getUpcomingHearings(lawyerId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCaseTitle()).isEqualTo("Sharma vs State");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown hearingId")
        void shouldThrowForUnknownHearing() {
            when(hearingRepository.findByIdAndLawyerId(any(), eq(lawyerId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    hearingService.getHearingById(UUID.randomUUID(), lawyerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Hearing not found or access denied");
        }
    }

    @Nested
    @DisplayName("Update and delete")
    class MutationTests {

        @Test
        @DisplayName("Should update hearing fields and preserve aiAssisted flag")
        void shouldUpdateHearingAndPreserveAiFlag() {
            var request = new HearingUpdateRequest();
            request.setHearingDate(LocalDate.of(2024, 8, 1));
            request.setOutcome("Updated outcome");
            request.setNextDate(LocalDate.of(2024, 10, 1));
            request.setActionItems("[\"New action\"]");

            when(hearingRepository.findByIdAndLawyerId(hearingId, lawyerId))
                    .thenReturn(Optional.of(existingHearing));
            when(hearingRepository.save(any(Hearing.class)))
                    .thenReturn(existingHearing);

            hearingService.updateHearing(hearingId, request, lawyerId);

            verify(hearingRepository).save(
                    argThat(h -> h.isAiAssisted())); // flag preserved
        }

        @Test
        @DisplayName("Should delete hearing when lawyer owns it")
        void shouldDeleteOwnedHearing() {
            when(hearingRepository.findByIdAndLawyerId(hearingId, lawyerId))
                    .thenReturn(Optional.of(existingHearing));

            hearingService.deleteHearing(hearingId, lawyerId);

            verify(hearingRepository).delete(existingHearing);
        }
    }
}