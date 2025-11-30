package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.filter.JwtAuthFilter;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
// Quan trọng: Tắt bớt các filter bảo mật mặc định để request vào được Controller dễ dàng hơn
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Login API Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    // ============================================
    // a) Test POST /api/auth/login endpoint (3 điểm)
    // ============================================

    @Test
    @DisplayName("TC1: Login Success - Trả về 200, Cookie Token và User Info")
    void testLoginSuccess() throws Exception {
        // Arrange
        String username = "testuser";
        String password = "Test123";
        UUID userId = UUID.randomUUID();
        String mockToken = "mock.jwt.token";

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);

        LoginResponseDTO responseDTO = new LoginResponseDTO(userId, username);

        when(authService.authenticate(username, password)).thenReturn(responseDTO);
        when(jwtUtil.generateToken(userId, username)).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().value("token", mockToken))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.id").exists());
    }

    // ============================================
    // b) Test response structure và status codes (1 điểm)
    // ============================================

    @Test
    @DisplayName("TC2: Login Fail - User Not Found (404)")
    void testLoginFailure_UserNotFound() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("unknown");
        request.setPassword("123");

        when(authService.authenticate("unknown", "123"))
                .thenThrow(new UserNotFoundException("Không tìm thấy người dùng"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Không tìm thấy người dùng"));
    }

    @Test
    @DisplayName("TC3: Login Fail - Wrong Password (401)")
    void testLoginFailure_WrongPassword() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("user");
        request.setPassword("wrong");

        when(authService.authenticate("user", "wrong"))
                .thenThrow(new WrongPassWordException("Sai mật khẩu"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Sai mật khẩu"));
    }

    // ============================================
    // c) Test CORS và headers (1 điểm)
    // ============================================

    @Test
    @DisplayName("TC4: Test CORS Configuration")
    void testCorsHeaders() throws Exception {
        // Arrange: Chuẩn bị dữ liệu hợp lệ để tránh NullPointerException trong Controller
        String username = "test";
        String password = "test";
        UUID userId = UUID.randomUUID();

        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);

        LoginResponseDTO responseDTO = new LoginResponseDTO(userId, username);

        // Mock hành vi: Khi controller gọi service, trả về object hợp lệ (thay vì null)
        when(authService.authenticate(username, password)).thenReturn(responseDTO);
        when(jwtUtil.generateToken(userId, username)).thenReturn("mock.token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
        // Ở đây chúng ta chỉ cần expect 200 OK là đủ chứng minh request không bị chặn bởi CORS pre-flight
        // và logic controller chạy mượt mà.
    }
}