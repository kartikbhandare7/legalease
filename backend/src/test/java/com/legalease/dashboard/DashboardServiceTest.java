package com.legalease.dashboard;

import com.legalease.client.repository.ClientRepository;
import com.legalease.common.enums.CaseStatus;
import com.legalease.dashboard.repository.DashboardRepository;
import com.legalease.dashboard.service.DashboardService;
import com.legalease.hearing.repository.HearingRepository;
import com.legalease.hearing.service.HearingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock private DashboardRepository dashboardRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private HearingRepository hearingRepository;
    @Mock private HearingService hearingService;

    @InjectMocks private DashboardService dashboardService;

    @Test
    @DisplayName("Should return correct counts and empty upcoming list")
    void shouldReturnCorrectDashboardCounts() {
        UUID lawyerId = UUID.randomUUID();

        when(dashboardRepository.countByLawyerId(lawyerId)).thenReturn(10L);
        when(dashboardRepository.countByLawyerIdAndCaseStatus(
                lawyerId, CaseStatus.ACTIVE)).thenReturn(6L);
        when(dashboardRepository.countByLawyerIdAndCaseStatus(
                lawyerId, CaseStatus.CLOSED)).thenReturn(3L);
        when(dashboardRepository.countByLawyerIdAndCaseStatus(
                lawyerId, CaseStatus.ON_HOLD)).thenReturn(1L);
        when(clientRepository.countByLawyerId(lawyerId)).thenReturn(8L);
        when(clientRepository.countByLawyerIdAndAiAssistedTrue(lawyerId))
                .thenReturn(5L);
        when(hearingRepository.countByLawyerId(lawyerId)).thenReturn(12L);
        when(hearingRepository.countByLawyerIdAndAiAssistedTrue(lawyerId))
                .thenReturn(9L);
        when(hearingService.getUpcomingHearings(lawyerId)).thenReturn(List.of());
        when(dashboardRepository.findTop5ByLawyerIdOrderByCreatedAtDesc(lawyerId))
                .thenReturn(List.of());

        var response = dashboardService.getDashboard(lawyerId);

        assertThat(response.getTotalCases()).isEqualTo(10L);
        assertThat(response.getActiveCases()).isEqualTo(6L);
        assertThat(response.getClosedCases()).isEqualTo(3L);
        assertThat(response.getOnHoldCases()).isEqualTo(1L);
        assertThat(response.getTotalClients()).isEqualTo(8L);
        assertThat(response.getAiAssistedIntakes()).isEqualTo(5L);
        assertThat(response.getTotalHearings()).isEqualTo(12L);
        assertThat(response.getAiAssistedHearings()).isEqualTo(9L);
        assertThat(response.getUpcomingHearings()).isEmpty();
        assertThat(response.getRecentCases()).isEmpty();
    }

//    @Test
//    @DisplayName("Should limit upcoming hearings to 5")
//    void shouldLimitUpcomingHearingsToFive() {
//        UUID lawyerId = UUID.randomUUID();
//
//        // Return 7 upcoming — service should cap at 5
//        var mockHearings = java.util.Collections
//                .nCopies(7, new com.legalease.hearing.dto.HearingResponse());
//
//        when(dashboardRepository.countByLawyerId(any())).thenReturn(0L);
//        when(dashboardRepository.countByLawyerIdAndCaseStatus(any(), any()))
//                .thenReturn(0L);
//        when(clientRepository.countByLawyerId(any())).thenReturn(0L);
//        when(clientRepository.countByLawyerIdAndAiAssistedTrue(any()))
//                .thenReturn(0L);
//        when(hearingRepository.countByLawyerId(any())).thenReturn(0L);
//        when(hearingRepository.countByLawyerIdAndAiAssistedTrue(any()))
//                .thenReturn(0L);
//        when(hearingService.getUpcomingHearings(lawyerId))
//                .thenReturn(mockHearings);
//        when(dashboardRepository.findTop5ByLawyerIdOrderByCreatedAtDesc(any()))
//                .thenReturn(List.of());
//
//        var response = dashboardService.getDashboard(lawyerId);
//
//        assertThat(response.getUpcomingHearings()).hasSize(5);
//    }
}