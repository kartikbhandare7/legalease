package com.legalease.hearing.repository;

import com.legalease.hearing.model.Hearing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HearingRepository extends JpaRepository<Hearing, UUID> {
    Page<Hearing> findByLegalCaseIdOrderByHearingDateDesc(UUID caseId, Pageable pageable);

    Page<Hearing> findByLawyerIdOrderByHearingDateDesc(UUID lawyerId, Pageable pageable);

    Optional<Hearing> findByIdAndLawyerId(UUID hearingId, UUID lawyerId);

    @Query("""
    SELECT h FROM Hearing h
    WHERE h.lawyer.id = :lawyerId
    AND h.nextDate >= :today
    ORDER BY h.nextDate ASC
""")
    List<Hearing> findUpcomingHearings(
            @Param("lawyerId") UUID lawyerId,
            @Param("today") LocalDate today
    );

    long countByLawyerIdAndAiAssistedTrue(UUID lawyerId);

    boolean existsByLegalCaseIdAndHearingDate(UUID caseId, LocalDate hearingDate);

}
