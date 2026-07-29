package com.legalease.dashboard.service;

import com.legalease.client.repository.ClientRepository;
import com.legalease.common.enums.CaseStatus;
import com.legalease.dashboard.dto.DashboardResponse;
import com.legalease.dashboard.dto.RecentCaseDto;
import com.legalease.dashboard.repository.DashboardRepository;
import com.legalease.hearing.repository.HearingRepository;
import com.legalease.hearing.service.HearingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final ClientRepository clientRepository;
    private final HearingRepository hearingRepository;
    private final HearingService hearingService;

    public DashboardResponse getDashboard(UUID lawyerId){
        long totalCases = dashboardRepository.countByLawyerId(lawyerId);
        long activeCases = dashboardRepository.countByLawyerIdAndCaseStatus(lawyerId , CaseStatus.ACTIVE);
        long closedCases = dashboardRepository.countByLawyerIdAndCaseStatus(lawyerId, CaseStatus.CLOSED);
        long onHoldCases = dashboardRepository.countByLawyerIdAndCaseStatus(lawyerId,  CaseStatus.ON_HOLD);

        long totalClients = clientRepository.countByLawyerId(lawyerId);
        long aiAssistedIntakes = clientRepository.countByLawyerIdAndAiAssistedTrue(lawyerId);

        long totalHearings = hearingRepository.countByLawyerId(lawyerId);
        long aiAssistedHearings = hearingRepository.countByLawyerIdAndAiAssistedTrue(lawyerId);

        var upcoming = hearingService.getUpcomingHearings(lawyerId)
                .stream()
                .limit(5)
                .toList();

        List<RecentCaseDto> recentCases = dashboardRepository
                .findTop5ByLawyerIdOrderByCreatedAtDesc(lawyerId)
                .stream()
                .map(c -> RecentCaseDto.builder()
                        .id(c.getId())
                        .caseTitle(c.getCaseTitle())
                        .caseStatus(c.getCaseStatus())
                        .caseType(c.getCaseType())
                        .courtName(c.getCourtName())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalCases(totalCases)
                .activeCases(activeCases)
                .closedCases(closedCases)
                .onHoldCases(onHoldCases)
                .totalClients(totalClients)
                .aiAssistedIntakes(aiAssistedIntakes)
                .totalHearings(totalHearings)
                .aiAssistedHearings(aiAssistedHearings)
                .upcomingHearings(upcoming)
                .recentCases(recentCases)
                .build();
    }
}
