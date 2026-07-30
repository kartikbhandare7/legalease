package com.legalease.admin.repository;

import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.UserRole;
import com.legalease.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<User, UUID> {

    // Pending lawyers — primary admin queue
    Page<User> findByRoleAndAccountStatusOrderByCreatedAtAsc(
            UserRole role,
            AccountStatus status,
            Pageable pageable
    );

    // Filter all users by role string — optional param
    Page<User> findByRoleOrderByCreatedAtDesc(
            UserRole role, Pageable pageable);

    // All users regardless of role — paginated
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Counts for admin dashboard
    long countByRole(UserRole role);
    long countByRoleAndAccountStatus(UserRole role, AccountStatus status);

    // Last 5 registrations — recent activity widget
    @Query("""
        SELECT u FROM User u
        ORDER BY u.createdAt DESC
        LIMIT 5
        """)
    List<User> findTop5ByOrderByCreatedAtDesc();
}