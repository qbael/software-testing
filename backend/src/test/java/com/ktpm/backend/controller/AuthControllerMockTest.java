package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testLoginSuccess() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        LoginResponseDTO loginResponse = new LoginResponseDTO(userId, "testuser");
        String mockToken = "mock.jwt.token";

        when(authService.authenticate("testuser", "Password123"))
                .thenReturn(loginResponse);
        when(jwtUtil.generateToken(userId, "testuser"))
                .thenReturn(mockToken);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(cookie().secure("token", true))
                .andExpect(cookie().path("token", "/"))
                .andExpect(cookie().maxAge("token", 24 * 60 * 60 * 3))
                .andReturn();

        Cookie jwtCookie = result.getResponse().getCookie("token");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEqualTo(mockToken);

        verify(authService, times(1)).authenticate("testuser", "Password123");
        verify(jwtUtil, times(1)).generateToken(userId, "testuser");
    }

    @Test
    void testLoginInvalidUsernameReturnsBadRequest() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("ab");
        loginRequest.setPassword("Password123");

        when(authService.authenticate("ab", "Password123"))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username hoặc mật khẩu không hợp lệ"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate("ab", "Password123");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLoginInvalidPasswordReturnsBadRequest() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("12345");

        when(authService.authenticate("testuser", "12345"))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username hoặc mật khẩu không hợp lệ"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate("testuser", "12345");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLoginUserNotFoundReturnsNotFound() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("Password123");

        when(authService.authenticate("nonexistent", "Password123"))
                .thenThrow(new UserNotFoundException("Không tìm thấy người dùng"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Không tìm thấy người dùng"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate("nonexistent", "Password123");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLoginWrongPasswordReturnsUnauthorized() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("WrongPass123");

        when(authService.authenticate("testuser", "WrongPass123"))
                .thenThrow(new WrongPassWordException("Sai mật khẩu"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Sai mật khẩu"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate("testuser", "WrongPass123");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLoginNullUsernameReturnsBadRequest() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername(null);
        loginRequest.setPassword("Password123");

        when(authService.authenticate(null, "Password123"))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username hoặc mật khẩu không hợp lệ"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate(isNull(), eq("Password123"));
    }

    @Test
    void testLoginNullPasswordReturnsBadRequest() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword(null);

        when(authService.authenticate("testuser", null))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username hoặc mật khẩu không hợp lệ"))
                .andExpect(cookie().doesNotExist("token"));

        verify(authService, times(1)).authenticate(eq("testuser"), isNull());
    }
}