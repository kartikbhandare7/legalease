package com.legalease.user.model;

import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.AuthProvider;
import com.legalease.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false,nullable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email" ,nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name ="auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column (name = "provider_id")
    private String providerId;

    @Column(name = "bar_concil_number")
    private String barConcilNumber;

    @Column(name = "certificate_path")
    private String certificatePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", unique = false)
    private AccountStatus accountStatus;

    @Column(name = "referral_code" , unique = true)
    private String referralCode;

    @Column(name = "invited_by")
    private UUID invitedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
