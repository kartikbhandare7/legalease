package com.legalease.admin.controller;

import com.legalease.admin.dto.AdminDashboardResponse;
import com.legalease.admin.dto.ApprovalRequest;
import com.legalease.admin.service.AdminService;
import com.legalease.common.security.CurrentUser;
import com.legalease.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// Every method in this controller is ADMIN only — declared at class level
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // Admin overview — pending count, total users, recent registrations
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    // All lawyers with PENDING status — main admin task
    @GetMapping("/pending-lawyers")
    public ResponseEntity<Page<UserResponse>> getPendingLawyers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                adminService.getPendingLawyers(page, size));
    }

    // All users — filter by role optionally
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                adminService.getAllUsers(role, page, size));
    }

    // Single user detail — admin clicks a pending lawyer to review
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    // Approve or reject a lawyer account
    @PatchMapping("/users/{userId}/approval")
    public ResponseEntity<UserResponse> updateApprovalStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody ApprovalRequest request,
            @CurrentUser UUID adminId) {
        return ResponseEntity.ok(
                adminService.updateApprovalStatus(userId, request, adminId));
    }

    // Hard delete — admin removes a user entirely
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @CurrentUser UUID adminId) {
        adminService.deleteUser(userId, adminId);
        return ResponseEntity.noContent().build();
    }
}