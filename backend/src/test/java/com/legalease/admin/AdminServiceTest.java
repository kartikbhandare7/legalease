package com.legalease.admin;

import com.legalease.admin.dto.ApprovalRequest;
import com.legalease.admin.repository.AdminRepository;
import com.legalease.admin.service.AdminService;
import com.legalease.common.enums.*;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.user.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Tests")
class AdminServiceTest {

    @Mock private AdminRepository adminRepository;
    @InjectMocks private AdminService adminService;

    private UUID adminId;
    private UUID lawyerId;
    private User pendingLawyer;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminId  = UUID.randomUUID();
        lawyerId = UUID.randomUUID();

        pendingLawyer = User.builder()
                .id(lawyerId)
                .fullName("Adv. Rahul Sharma")
                .email("rahul@legalease.com")
                .role(UserRole.ROLE_LAWYER)
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.PENDING)
                .barCouncilNumber("MH/1234/2020")
                .build();

        adminUser = User.builder()
                .id(adminId)
                .fullName("Super Admin")
                .email("admin@legalease.com")
                .role(UserRole.ROLE_ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Approve / Reject lawyer")
    class ApprovalTests {

        @Test
        @DisplayName("Should approve pending lawyer successfully")
        void shouldApprovePendingLawyer() {
            var request = new ApprovalRequest();
            request.setStatus(AccountStatus.ACTIVE);

            when(adminRepository.findById(lawyerId))
                    .thenReturn(Optional.of(pendingLawyer));
            when(adminRepository.save(any(User.class)))
                    .thenReturn(pendingLawyer);

            var response = adminService.updateApprovalStatus(
                    lawyerId, request, adminId);

            verify(adminRepository).save(
                    argThat(u -> u.getAccountStatus() == AccountStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should reject pending lawyer successfully")
        void shouldRejectPendingLawyer() {
            var request = new ApprovalRequest();
            request.setStatus(AccountStatus.REJECTED);
            request.setRejectionReason("Certificate appears invalid");

            when(adminRepository.findById(lawyerId))
                    .thenReturn(Optional.of(pendingLawyer));
            when(adminRepository.save(any(User.class)))
                    .thenReturn(pendingLawyer);

            adminService.updateApprovalStatus(lawyerId, request, adminId);

            verify(adminRepository).save(
                    argThat(u -> u.getAccountStatus() == AccountStatus.REJECTED));
        }

        @Test
        @DisplayName("Should throw BadRequestException when setting status to PENDING")
        void shouldThrowWhenSettingStatusToPending() {
            var request = new ApprovalRequest();
            request.setStatus(AccountStatus.PENDING);

            when(adminRepository.findById(lawyerId))
                    .thenReturn(Optional.of(pendingLawyer));

            assertThatThrownBy(() ->
                    adminService.updateApprovalStatus(lawyerId, request, adminId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot manually set status to PENDING");
        }

        @Test
        @DisplayName("Should throw BadRequestException when approving an admin")
        void shouldThrowWhenModifyingAdminAccount() {
            var request = new ApprovalRequest();
            request.setStatus(AccountStatus.ACTIVE);

            when(adminRepository.findById(adminId))
                    .thenReturn(Optional.of(adminUser));

            assertThatThrownBy(() ->
                    adminService.updateApprovalStatus(adminId, request, adminId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot modify approval status of an admin");
        }
    }

    @Nested
    @DisplayName("Delete user")
    class DeleteTests {

        @Test
        @DisplayName("Should delete lawyer successfully")
        void shouldDeleteLawyerSuccessfully() {
            when(adminRepository.findById(lawyerId))
                    .thenReturn(Optional.of(pendingLawyer));

            adminService.deleteUser(lawyerId, adminId);

            verify(adminRepository).delete(pendingLawyer);
        }

        @Test
        @DisplayName("Should throw when admin tries to delete themselves")
        void shouldThrowWhenDeletingSelf() {
            when(adminRepository.findById(adminId))
                    .thenReturn(Optional.of(adminUser));

            assertThatThrownBy(() ->
                    adminService.deleteUser(adminId, adminId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("cannot delete their own account");

            verify(adminRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw when admin tries to delete another admin")
        void shouldThrowWhenDeletingAnotherAdmin() {
            UUID anotherAdminId = UUID.randomUUID();
            User anotherAdmin = User.builder()
                    .id(anotherAdminId)
                    .role(UserRole.ROLE_ADMIN)
                    .build();

            when(adminRepository.findById(anotherAdminId))
                    .thenReturn(Optional.of(anotherAdmin));

            assertThatThrownBy(() ->
                    adminService.deleteUser(anotherAdminId, adminId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cannot delete another admin");

            verify(adminRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for unknown userId")
        void shouldThrowForUnknownUser() {
            when(adminRepository.findById(any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    adminService.deleteUser(UUID.randomUUID(), adminId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("Admin dashboard")
    class DashboardTests {

        @Test
        @DisplayName("Should return correct admin dashboard counts")
        void shouldReturnCorrectCounts() {
            when(adminRepository.countByRole(UserRole.ROLE_LAWYER)).thenReturn(20L);
            when(adminRepository.countByRole(UserRole.ROLE_CLERK)).thenReturn(5L);
            when(adminRepository.countByRoleAndAccountStatus(
                    UserRole.ROLE_LAWYER, AccountStatus.PENDING)).thenReturn(3L);
            when(adminRepository.countByRoleAndAccountStatus(
                    UserRole.ROLE_LAWYER, AccountStatus.ACTIVE)).thenReturn(15L);
            when(adminRepository.countByRoleAndAccountStatus(
                    UserRole.ROLE_LAWYER, AccountStatus.REJECTED)).thenReturn(2L);
            when(adminRepository.findTop5ByOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            var response = adminService.getAdminDashboard();

            assertThat(response.getTotalUsers()).isEqualTo(25L);
            assertThat(response.getPendingApprovals()).isEqualTo(3L);
            assertThat(response.getApprovedLawyers()).isEqualTo(15L);
            assertThat(response.getRejectedLawyers()).isEqualTo(2L);
        }
    }
}