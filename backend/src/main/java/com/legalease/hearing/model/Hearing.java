package com.legalease.hearing.model;

import com.legalease.cases.model.Case;
import com.legalease.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hearings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case legalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer-id", nullable = false)
    private User lawyer;

    @Column(name = "hearing_date", nullable = false)
    private LocalDate hearingDate;

    @Column(name = "outcome", columnDefinition = "TEXT")
    private String outcome;

    @Column(name = "next_date")
    private LocalDate nextDate;

    @Column(name = "action-items", columnDefinition = "TEXT")
    private String actionItems;

    @Column(name = "raw_note", columnDefinition = "TEXT")
    private String rawNote;

    @Column(name = "ai_assisted", nullable = false)
    private boolean aiAssisted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
