package com.legalease.cases.repository;

import com.legalease.cases.model.Case;
import com.legalease.common.enums.CaseStatus;
import com.openai.core.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<Case, UUID> {

    Page<Case> findByLawyerIdOrderByCreatedAtDesc(UUID lawyerId, Pageable pageable);

    Page<Case> findByLawyerIdAndCaseStatusOrderByCreatedAtDesc(UUID lawyerId, CaseStatus status, Pageable pageable);

    Optional<Case> findByIdAndLawyerId(UUID caseId, UUID lawyerId);

    Long courtByLawyerIdAndCaseStatus(UUID lawyerId, CaseStatus status);

    @Query("""
        SELECT c FROM Case c
        WHERE c.lawyer.id = :lawyerId
        AND LOWER(c.caseTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY c.createdAt DESC
        """)
    Page<Case> searchByTitle(
            @Param("lawyerId") UUID lawyerId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
