package com.legalease.auth.service;

import com.legalease.auth.dto.AuthResponse;
import com.legalease.auth.dto.LoginRequest;
import com.legalease.auth.dto.RegisterRequest;
import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.AuthProvider;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.ResourceNotFoundException;
import com.legalease.common.exception.UnauthorizedException;
import com.legalease.common.security.JwtService;
import com.legalease.user.model.User;
import com.legalease.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final String UPLOAD_DIR = "Upload/certificates/";

    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile certificate) throws IOException {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        if(request.getRole() == UserRole.ROLE_LAWYER){
            validateLawyerRegistration(request , certificate);
        }else if(request.getRole() == UserRole.ROLE_CLERK){
            validateClerkRegistration(request);
        }

        if(request.getPassword() == null || request.getPassword().isBlank()){
            throw new BadRequestException("Password is required for registration");
        }

        User.UserBuilder userBuilder = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .authProvider(AuthProvider.LOCAL)
                .accountStatus(AccountStatus.PENDING);

        if(request.getRole() == UserRole.ROLE_LAWYER){
            String certPath = saveCertificate(certificate , request.getEmail());
            userBuilder
                    .barConcilNumber(request.getBarCouncilNumber())
                    .certificatePath(certPath)
                    .referralCode(generateReferralCode());
        }
        if(request.getRole() == UserRole.ROLE_CLERK){
            User invitingLawyer = userRepository
                    .findByReferralCode(request.getReferralCode())
                    .orElseThrow(() -> new BadRequestException("Invalid referral Code"));

            userBuilder.invitedBy(invitingLawyer.getId());

            userBuilder.accountStatus(AccountStatus.ACTIVE);
        }
        User saved = userRepository.save(userBuilder.build());

        if(saved.getRole() == UserRole.ROLE_LAWYER){
            return AuthResponse.builder()
                    .email(saved.getEmail())
                    .fullName(saved.getFullName())
                    .role(saved.getRole())
                    .accountStatus(saved.getAccountStatus())
                    .userId(saved.getId())
                    .approval(false)
                    .build();
        }
        String token = jwtService.generateToken(
                saved.getEmail(), saved.getRole().name() , saved.getId()
        );
        return AuthResponse.builder()
                .token(token)
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .role(saved.getRole())
                .accountStatus(AccountStatus.ACTIVE)
                .userId(saved.getId())
                .approval(true)
                .build();
    }

    public AuthResponse login(LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        }catch(BadCredentialsException e){
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if(user.getAccountStatus() == (AccountStatus.PENDING)){
            return AuthResponse.builder()
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .accountStatus(AccountStatus.PENDING)
                    .userId(user.getId())
                    .approval(false)
                    .build();
        }
        if(user.getAccountStatus() == (AccountStatus.REJECTED)){
            throw new UnauthorizedException("Your account has been rejected. Contact support.");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .userId(user.getId())
                .approval(true)
                .build();
    }

    private void validateLawyerRegistration(RegisterRequest request, MultipartFile certificate)  {
        if(request.getBarCouncilNumber() == null || request.getBarCouncilNumber().isBlank()){
            throw new BadRequestException("Bar Council enrollment number is required for lawyers");
        }
        if(certificate == null || certificate.isEmpty()){
            throw new BadRequestException("Enrollment certificate upload is required for lawyers");
        }

        String contentType = certificate.getContentType();
        if(contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image"))){
            throw new BadRequestException("Certificate must be a PDF or Image file");
        }
    }

    private void validateClerkRegistration(RegisterRequest request){
        if(request.getReferralCode() == null || request.getReferralCode().isBlank()){
            throw new BadRequestException("Referral code us required for clerk registration");
        }
    }
    private String saveCertificate(MultipartFile file,
                                   String email) throws IOException {
        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Unique filename — email prefix + UUID to avoid collisions
        String filename = email.replace("@", "_")
                + "_" + UUID.randomUUID() + "_cert."
                + getExtension(file.getOriginalFilename());

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return filePath.toString();
    }
    private String getExtension(String filename) {
        if (filename == null) return "pdf";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "pdf";
    }

    private String generateReferralCode() {
        return "LAW-" + UUID.randomUUID()
                .toString().substring(0, 6).toUpperCase();
    }
}
