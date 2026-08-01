package com.legalease.dashboard.dto;

import com.legalease.hearing.dto.HearingResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private long totalCases;
    private long activeCases;
    private long closedCases;
    private long onHoldCases;

    private long totalClients;
    private long aiAssistedIntakes;

    private long totalHearings;
    private long aiAssistedHearings;

    private List<HearingResponse> upcomingHearings;

    private List<RecentCaseDto> recentCases;

}
