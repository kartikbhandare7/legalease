package com.legalease.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalease.auth.controller.AuthController;
import com.legalease.auth.dto.AuthResponse;
import com.legalease.auth.dto.LoginRequest;
import com.legalease.auth.service.AuthService;
import com.legalease.common.enums.AccountStatus;
import com.legalease.common.enums.UserRole;
import com.legalease.common.exception.BadRequestException;
import com.legalease.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean  private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login — should return 200 with token for valid credentials")
    void shouldReturn200WithTokenOnValidLogin() throws Exception {
        var request = new LoginRequest();
        request.setEmail("rahul@legalease.com");
        request.setPassword("SecurePass@123");

        var mockResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .email("rahul@legalease.com")
                .role(UserRole.ROLE_LAWYER)
                .accountStatus(AccountStatus.ACTIVE)
                .userId(UUID.randomUUID())
                .approval(true)
                .build();

        when(authService.login(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/auth/login — should return 400 when email blank")
    void shouldReturn400WhenEmailBlank() throws Exception {
        var request = new LoginRequest();
        request.setEmail("");
        request.setPassword("pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register — should return 200 for valid lawyer multipart")
    void shouldReturn200ForValidLawyerRegistration() throws Exception {
        var pendingResponse = AuthResponse.builder()
                .email("rahul@legalease.com")
                .role(UserRole.ROLE_LAWYER)
                .accountStatus(AccountStatus.PENDING)
                .approval(false)
                .userId(UUID.randomUUID())
                .build();

        when(authService.register(any(), any())).thenReturn(pendingResponse);

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json",
                """
                {
                  "fullName": "Adv. Rahul Sharma",
                  "email": "rahul@legalease.com",
                  "password": "SecurePass@123",
                  "role": "ROLE_LAWYER",
                  "barCouncilNumber": "MH/1234/2020"
                }
                """.getBytes()
        );

        MockMultipartFile cert = new MockMultipartFile(
                "certificate", "cert.pdf",
                "application/pdf", "PDF".getBytes()
        );

        mockMvc.perform(multipart("/api/auth/register")
                        .file(data)
                        .file(cert))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("PENDING"))
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register — should return 400 on duplicate email")
    void shouldReturn400OnDuplicateEmail() throws Exception {
        when(authService.register(any(), any()))
                .thenThrow(new BadRequestException("Email already registered"));

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json",
                """
                {
                  "fullName": "Adv. Rahul",
                  "email": "existing@legalease.com",
                  "password": "pass",
                  "role": "ROLE_LAWYER",
                  "barCouncilNumber": "MH/999/2020"
                }
                """.getBytes()
        );

        mockMvc.perform(multipart("/api/auth/register").file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }
}