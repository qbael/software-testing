package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class KtraAuthController {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void loginSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        LoginResponseDTO loginResponse = new LoginResponseDTO(userId, "testuser");
        String mockToken = "mock.jwt.token";

        when(authService.authenticate("testuser", "Password123")).thenReturn(loginResponse);
        when(jwtUtil.generateToken(userId, "testuser")).thenReturn(mockToken);

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
                .andExpect(cookie().maxAge("token", 24*60*60*3))
                .andReturn();

        Cookie jwtCookie = result.getResponse().getCookie("token");
        assertNotNull(jwtCookie);
        assertEquals(mockToken, jwtCookie.getValue());
    }

    @Test
    void loginUsernameNotValid() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("ab");
        loginRequest.setPassword("Password123");

        when(authService.authenticate("ab", "Password123"))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).authenticate("ab", "Password123");
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void loginPasswordNotValid() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("ok");

        when(authService.authenticate("testuser", "ok"))
                .thenThrow(new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).authenticate("testuser", "ok");
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void loginUserNotFound() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("nonexist");
        loginRequest.setPassword("Password1");

        when(authService.authenticate("nonexist", "Password1"))
                .thenThrow(new UserNotFoundException("Không tìm thấy người dùng"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());

        verify(authService, times(1)).authenticate("nonexist", "Password1");
        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
