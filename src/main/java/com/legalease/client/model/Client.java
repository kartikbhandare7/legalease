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
@Table(name="clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false,nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case case_;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_id",nullable = false)
    private User lawyer;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "opposing_party")
    private String opposingParty;

    @Column(name = "case_background", columnDefinition = "TEXT")
    private String caseBackground;

    @Column(name = "raw_intake_note", columnDefinition = "TEXT")
    private String rawIntakeNote;

    @Column(name = "ai_assisted", nullable = false)
    private boolean aiAssisted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
