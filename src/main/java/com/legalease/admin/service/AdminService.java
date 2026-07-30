package com.legalease.admin.service;

import com.legalease.admin.dto.AdminDashboardResponse;
import com.legalease.admin.dto.ApprovalRequest;
import com.legalease.admin.repository.AdminRepository;
import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.dto.UserResponse;
import com.legalease.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;

    // ── DASHBOARD ─────────────────────────────────────────────────────────────

    public AdminDashboardResponse getAdminDashboard() {
        long totalLawyers    = adminRepository.countByRole(UserRole.ROLE_LAWYER);
        long totalClerks     = adminRepository.countByRole(UserRole.ROLE_CLERK);
        long pendingLawyers  = adminRepository.countByRoleAndAccountStatus(
                UserRole.ROLE_LAWYER, AccountStatus.PENDING);
        long approvedLawyers = adminRepository.countByRoleAndAccountStatus(
                UserRole.ROLE_LAWYER, AccountStatus.ACTIVE);
        long rejectedLawyers = adminRepository.countByRoleAndAccountStatus(
                UserRole.ROLE_LAWYER, AccountStatus.REJECTED);

        var recentRegistrations = adminRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return AdminDashboardResponse.builder()
                .totalUsers(totalLawyers + totalClerks)
                .totalLawyers(totalLawyers)
                .totalClerks(totalClerks)
                .pendingApprovals(pendingLawyers)
                .approvedLawyers(approvedLawyers)
                .rejectedLawyers(rejectedLawyers)
                .recentRegistrations(recentRegistrations)
                .build();
    }

    // ── PENDING LAWYERS ───────────────────────────────────────────────────────

    public Page<UserResponse> getPendingLawyers(int page, int size) {
        return adminRepository
                .findByRoleAndAccountStatusOrderByCreatedAtAsc(
                        UserRole.ROLE_LAWYER,
                        AccountStatus.PENDING,
                        PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    // ── ALL USERS ─────────────────────────────────────────────────────────────

    public Page<UserResponse> getAllUsers(String role, int page, int size) {
        var pageable = PageRequest.of(page, size);

        if (role != null && !role.isBlank()) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                return adminRepository
                        .findByRoleOrderByCreatedAtDesc(userRole, pageable)
                        .map(this::mapToResponse);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + role);
            }
        }

        return adminRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    // ── SINGLE USER ───────────────────────────────────────────────────────────

    public UserResponse getUserById(UUID userId) {
        return mapToResponse(findUserOrThrow(userId));
    }

    // ── APPROVE / REJECT ──────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateApprovalStatus(UUID userId,
                                             ApprovalRequest request,
                                             UUID adminId) {
        User user = findUserOrThrow(userId);

        // Admin cannot approve/reject other admins
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new BadRequestException(
                    "Cannot modify approval status of an admin account");
        }

        // Admin cannot set status back to PENDING manually
        if (request.getStatus() == AccountStatus.PENDING) {
            throw new BadRequestException(
                    "Cannot manually set status to PENDING");
        }

        // Approving an already approved user — warn but allow
        // Useful when admin re-approves after temporary suspension
        if (user.getAccountStatus() == AccountStatus.ACTIVE &&
                request.getStatus() == AccountStatus.ACTIVE) {
            log.warn("Admin {} re-approved already active user {}",
                    adminId, userId);
        }

        user.setAccountStatus(request.getStatus());

        log.info("Admin {} set user {} status to {}",
                adminId, userId, request.getStatus());

        return mapToResponse(adminRepository.save(user));
    }

    // ── DELETE USER ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteUser(UUID userId, UUID adminId) {
        User user = findUserOrThrow(userId);

        // Admin cannot delete themselves
        if (userId.equals(adminId)) {
            throw new BadRequestException("Admin cannot delete their own account");
        }

        // Admin cannot delete other admins
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new BadRequestException("Cannot delete another admin account");
        }

        adminRepository.delete(user);
        log.info("Admin {} deleted user {}", adminId, userId);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private User findUserOrThrow(UUID userId) {
        return adminRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private UserResponse mapToResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .authProvider(u.getAuthProvider())
                .accountStatus(u.getAccountStatus())
                .barCouncilNumber(u.getBarCouncilNumber())
                .certificatePath(u.getCertificatePath())
                .referralCode(u.getReferralCode())
                .createdAt(u.getCreatedAt())
                .build();
    }
}