package com.legalease.client.model;

import com.legalease.cases.model.Case;
import com.legalease.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Client always belongs to a case
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case legalCase;

    // Direct lawyer link — fast dashboard queries without joining cases
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id", nullable = false)
    private User lawyer;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    // Opposing party — AI extracts from raw intake note
    @Column(name = "opposing_party")
    private String opposingParty;

    // Full background story — AI fills from lawyer's rough description
    @Column(name = "case_background", columnDefinition = "TEXT")
    private String caseBackground;

    // Raw note lawyer typed before AI parsed — kept for audit trail
    @Column(name = "raw_intake_note", columnDefinition = "TEXT")
    private String rawIntakeNote;

    // True if intake was filled via AI log — useful for analytics
    @Column(name = "ai_assisted", nullable = false)
    private boolean aiAssisted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}