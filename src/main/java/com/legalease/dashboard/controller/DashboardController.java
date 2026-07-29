package com.legalease.dashboard.controller;

import com.legalease.common.security.CurrentUser;
import com.legalease.dashboard.dto.DashboardResponse;
import com.legalease.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LAWYER','CLERK')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @CurrentUser UUID lawyerId) {
        return ResponseEntity.ok(dashboardService.getDashboard(lawyerId));
    }
}
