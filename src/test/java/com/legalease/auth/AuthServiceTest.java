package com.legalease.auth;

import com.legalease.auth.dto.LoginRequest;
import com.legalease.auth.dto.RegisterRequest;
import com.legalease.auth.service.AuthService;
import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.AuthProvider;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.UnauthorizedException;
import com.legalease.common.security.JwtService;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Reusable test data
    private RegisterRequest lawyerRequest;
    private RegisterRequest clerkRequest;
    private MockMultipartFile validCertificate;
    private User activeLawyer;
    private User pendingLawyer;
    private User activeClerk;

    @BeforeEach
    void setUp() {
        // Lawyer registration request
        lawyerRequest = new RegisterRequest();
        lawyerRequest.setFullName("Adv. Rahul Sharma");
        lawyerRequest.setEmail("rahul@legalease.com");
        lawyerRequest.setPassword("SecurePass@123");
        lawyerRequest.setRole(UserRole.ROLE_LAWYER);
        lawyerRequest.setBarCouncilNumber("MH/1234/2020");

        // Clerk registration request
        clerkRequest = new RegisterRequest();
        clerkRequest.setFullName("Priya Clerk");
        clerkRequest.setEmail("priya@legalease.com");
        clerkRequest.setPassword("ClerkPass@123");
        clerkRequest.setRole(UserRole.ROLE_CLERK);
        clerkRequest.setReferralCode("LAW-ABC123");

        // Valid PDF certificate file
        validCertificate = new MockMultipartFile(
                "certificate",
                "enrollment.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Active lawyer — approved by admin
        activeLawyer = User.builder()
                .id(UUID.randomUUID())
                .email("rahul@legalease.com")
                .fullName("Adv. Rahul Sharma")
                .role(UserRole.ROLE_LAWYER)
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.ACTIVE)
                .password("encoded-password")
                .build();

        // Pending lawyer — not yet approved
        pendingLawyer = User.builder()
                .id(UUID.randomUUID())
                .email("pending@legalease.com")
                .fullName("Adv. Pending")
                .role(UserRole.ROLE_LAWYER)
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.PENDING)
                .password("encoded-password")
                .build();

        // Active clerk
        activeClerk = User.builder()
                .id(UUID.randomUUID())
                .email("priya@legalease.com")
                .fullName("Priya Clerk")
                .role(UserRole.ROLE_CLERK)
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.ACTIVE)
                .referralCode("LAW-ABC123")
                .build();
    }

    // ── REGISTER TESTS ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Register — Lawyer")
    class LawyerRegistrationTests {

        @Test
        @DisplayName("Should register lawyer successfully and return PENDING status")
        void shouldRegisterLawyerAndReturnPendingStatus() throws IOException {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
            when(userRepository.save(any(User.class))).thenReturn(pendingLawyer);

            var response = authService.register(lawyerRequest, validCertificate);

            // Lawyer must always start PENDING — never auto-approved
            assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.PENDING);
            assertThat(response.isApproval()).isFalse();
            assertThat(response.getToken()).isNull(); // no token until approved

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already exists")
        void shouldThrowWhenEmailAlreadyRegistered() {
            when(userRepository.existsByEmail(lawyerRequest.getEmail()))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    authService.register(lawyerRequest, validCertificate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already registered");

            // Must never save if email exists
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when Bar Council number missing")
        void shouldThrowWhenBarCouncilNumberMissing() {
            lawyerRequest.setBarCouncilNumber(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            assertThatThrownBy(() ->
                    authService.register(lawyerRequest, validCertificate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Bar Council enrollment number");
        }

        @Test
        @DisplayName("Should throw BadRequestException when certificate not uploaded")
        void shouldThrowWhenCertificateNotUploaded() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            assertThatThrownBy(() ->
                    authService.register(lawyerRequest, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("certificate upload is required");
        }

        @Test
        @DisplayName("Should throw BadRequestException when certificate is wrong type")
        void shouldThrowWhenCertificateTypeInvalid() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            MockMultipartFile invalidFile = new MockMultipartFile(
                    "certificate", "cert.exe",
                    "application/octet-stream", "binary".getBytes()
            );

            assertThatThrownBy(() ->
                    authService.register(lawyerRequest, invalidFile))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PDF or image");
        }
    }

    @Nested
    @DisplayName("Register — Clerk")
    class ClerkRegistrationTests {

        @Test
        @DisplayName("Should register clerk and return ACTIVE with JWT token")
        void shouldRegisterClerkWithTokenImmediately() throws IOException {
            User invitingLawyer = User.builder()
                    .id(UUID.randomUUID())
                    .referralCode("LAW-ABC123")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.findByReferralCode("LAW-ABC123"))
                    .thenReturn(Optional.of(invitingLawyer));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
            when(userRepository.save(any(User.class))).thenReturn(activeClerk);
            when(jwtService.generateToken(anyString(), anyString(), any(UUID.class)))
                    .thenReturn("clerk-jwt-token");

            var response = authService.register(clerkRequest, null);

            // Clerk is auto-approved — gets token immediately
            assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(response.isApproval()).isTrue();
            assertThat(response.getToken()).isEqualTo("clerk-jwt-token");
        }

        @Test
        @DisplayName("Should throw BadRequestException when referral code is invalid")
        void shouldThrowWhenReferralCodeInvalid() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.findByReferralCode(anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    authService.register(clerkRequest, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid referral code");
        }

        @Test
        @DisplayName("Should throw BadRequestException when referral code missing")
        void shouldThrowWhenReferralCodeMissing() {
            clerkRequest.setReferralCode(null);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            assertThatThrownBy(() ->
                    authService.register(clerkRequest, null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Referral code is required");
        }
    }

    // ── LOGIN TESTS ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login active lawyer and return JWT token")
        void shouldLoginActiveLawyerSuccessfully() {
            var request = new LoginRequest();
            request.setEmail("rahul@legalease.com");
            request.setPassword("SecurePass@123");

            when(userRepository.findByEmail("rahul@legalease.com"))
                    .thenReturn(Optional.of(activeLawyer));
            when(jwtService.generateToken(anyString(), anyString(), any(UUID.class)))
                    .thenReturn("lawyer-jwt-token");

            var response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("lawyer-jwt-token");
            assertThat(response.isApproval()).isTrue();
            assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);

            // AuthenticationManager must be called to verify credentials
            verify(authenticationManager).authenticate(
                    any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should return PENDING response when lawyer not yet approved")
        void shouldReturnPendingResponseForPendingLawyer() {
            var request = new LoginRequest();
            request.setEmail("pending@legalease.com");
            request.setPassword("pass");

            when(userRepository.findByEmail("pending@legalease.com"))
                    .thenReturn(Optional.of(pendingLawyer));

            var response = authService.login(request);

            // No token for pending lawyers
            assertThat(response.getToken()).isNull();
            assertThat(response.isApproval()).isFalse();
            assertThat(response.getAccountStatus()).isEqualTo(AccountStatus.PENDING);

            verify(jwtService, never())
                    .generateToken(anyString(), anyString(), any(UUID.class));
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for rejected account")
        void shouldThrowForRejectedAccount() {
            var rejectedUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("rejected@legalease.com")
                    .accountStatus(AccountStatus.REJECTED)
                    .role(UserRole.ROLE_LAWYER)
                    .build();

            var request = new LoginRequest();
            request.setEmail("rejected@legalease.com");
            request.setPassword("pass");

            when(userRepository.findByEmail("rejected@legalease.com"))
                    .thenReturn(Optional.of(rejectedUser));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("rejected");
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for wrong password")
        void shouldThrowForWrongPassword() {
            var request = new LoginRequest();
            request.setEmail("rahul@legalease.com");
            request.setPassword("wrong-password");

            // AuthenticationManager throws this on bad credentials
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid email or password");

            // Must never reach the DB on bad credentials
            verify(userRepository, never()).findByEmail(anyString());
        }
    }
}