package com.legalease.admin.dto;

import com.legalease.user.dto.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalLawyers;
    private long totalClerks;
    private long pendingApprovals;
    private long approvedLawyers;
    private long rejectedLawyers;

    private List<UserResponse> recentRegistrations;
}
