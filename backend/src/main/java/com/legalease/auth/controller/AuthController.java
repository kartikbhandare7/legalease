package com.legalease.auth.controller;

import com.legalease.auth.dto.AuthResponse;
import com.legalease.auth.dto.LoginRequest;
import com.legalease.auth.dto.RegisterRequest;
import com.legalease.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestPart("data")RegisterRequest request, @RequestPart(value = "certificate", required = false)
            MultipartFile certificate
            ) throws IOException {
        return ResponseEntity.ok(authService.register(request, certificate));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

}
