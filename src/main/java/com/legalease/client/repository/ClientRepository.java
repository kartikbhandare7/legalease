package com.legalease.client.repository;

import com.legalease.client.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    // All clients under a specific case
    Page<Client> findByCaseIdOrderByCreatedAtDesc(
            UUID caseId, Pageable pageable);

    // All clients for a lawyer — cross-case dashboard view
    Page<Client> findByLawyerIdOrderByCreatedAtDesc(
            UUID lawyerId, Pageable pageable);

    // Ownership check before any mutation
    Optional<Client> findByIdAndLawyerId(UUID clientId, UUID lawyerId);

    // Count AI-assisted intakes — portfolio metric, shows AI adoption
    long countByLawyerIdAndAiAssistedTrue(UUID lawyerId);

    // Search by client name across lawyer's entire client base
    @Query("""
        SELECT c FROM Client c
        WHERE c.lawyer.id = :lawyerId
        AND LOWER(c.clientName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY c.createdAt DESC
        """)
    Page<Client> searchByClientName(
            @Param("lawyerId") UUID lawyerId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Check if client already exists for this case — avoid duplicates
    boolean existsByCaseIdAndClientNameIgnoreCase(UUID caseId, String clientName);
}