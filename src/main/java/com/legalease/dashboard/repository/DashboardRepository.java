package com.legalease.dashboard.repository;

import com.legalease.cases.model.Case;
import com.legalease.common.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Case, UUID> {

    long countByLawyerId(UUID lawyerId);

    long countByLawyerIdAndCaseStatus(UUID lawyerId, CaseStatus status);

    @Query("""
        SELECT c FROM Case c
        WHERE c.lawyer.id = :lawyerId
        ORDER BY c.createdAt DESC
        LIMIT 5
""")
    List<Case> findTop5ByLawyerIdOrderByCreatedAtDesc(@Param ("lawyerId") UUID lawyerId);
}
