package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;
    private LoginResponseDTO loginResponse;
    private UUID testUserId;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        loginResponse = new LoginResponseDTO(testUserId, "testuser");
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /api/auth/login - Login thành công")
    void authenticate_Success() throws Exception {
        // Arrange
        when(authService.loginUser(anyString(), anyString())).thenReturn(loginResponse);
        when(jwtUtil.generateToken(any(UUID.class), anyString())).thenReturn(testToken);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().secure("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().maxAge("jwt", 24 * 60 * 60 * 3))
                .andReturn();

        // Verify mock interactions
        verify(authService, times(1)).loginUser("testuser", "Password123");
        verify(jwtUtil, times(1)).generateToken(testUserId, "testuser");
        verifyNoMoreInteractions(authService, jwtUtil);

        // Verify cookie value
        Cookie cookie = result.getResponse().getCookie("jwt");
        assertNotNull(cookie);
        assertEquals(testToken, cookie.getValue());
    }

    @Test
    @DisplayName("POST /api/auth/login - Username không tồn tại")
    void authenticate_UsernameNotFound() throws Exception {
        // Arrange
        when(authService.loginUser(anyString(), anyString()))
                .thenThrow(new UserNotFoundException("Không tìm thấy người dùng"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").doesNotExist());

        // Verify mock interactions
        verify(authService, times(1)).loginUser("testuser", "Password123");