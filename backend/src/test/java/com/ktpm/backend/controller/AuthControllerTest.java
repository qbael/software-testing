package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    // ==================== LOGIN TESTS ====================

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        LoginResponseDTO loginResponse = new LoginResponseDTO(userId, "testuser");
        String mockToken = "mock.jwt.token";

        when(authService.loginUser("testuser", "Password123"))
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
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().secure("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().maxAge("jwt", 24 * 60 * 60 * 3))
                .andReturn();

        // Verify
        verify(authService, times(1)).loginUser("testuser", "Password123");
        verify(jwtUtil, times(1)).generateToken(userId, "testuser");

        Cookie jwtCookie = result.getResponse().getCookie("jwt");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEqualTo(mockToken);
    }

    @Test
    void testLogin_InvalidUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("ab");
        loginRequest.setPassword("Password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // Verify - service should not be called
        verify(authService, never()).loginUser(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLogin_InvalidPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("12345");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, never()).loginUser(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLogin_UsernameNotFound_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("Password123");

        // Controller catches UsernameNotFoundException (from Spring Security)
        when(authService.loginUser("nonexistent", "Password123"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("jwt"));

        // Verify
        verify(authService, times(1)).loginUser("nonexistent", "Password123");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    @Test
    void testLogin_WrongPassword_ReturnsUnauthorized() throws Exception {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("WrongPass123");

        when(authService.loginUser("testuser", "WrongPass123"))
                .thenThrow(new WrongPassWordException("Wrong password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist("jwt"));

        // Verify
        verify(authService, times(1)).loginUser("testuser", "WrongPass123");
        verify(jwtUtil, never()).generateToken(any(UUID.class), anyString());
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Verify
        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_BlankUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_InvalidUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("ab");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_InvalidPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("12345");
        registerRequest.setVerifyPassword("12345");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, never()).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_UsernameExists_ReturnsConflict() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("existinguser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UsernameExistedException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());

        // Verify
        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_PasswordMismatch_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("DifferentPass123");

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new VerifyPasswordNotMatch("Passwords do not match"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_ServiceReturnsFalse_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Verify
        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    // ==================== GET CURRENT USER TESTS ====================

    @Test
    void testGetCurrentUser_Success() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        String mockToken = "valid.jwt.token";
        LoginResponseDTO currentUser = new LoginResponseDTO(userId, "testuser");

        when(jwtUtil.validateToken(mockToken)).thenReturn(true);
        when(authService.getCurrentUser(mockToken)).thenReturn(currentUser);

        // Act & Assert
        mockMvc.perform(get("/api/auth/current")
                        .cookie(new Cookie("jwt", mockToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));

        // Verify
        verify(jwtUtil, times(1)).validateToken(mockToken);
        verify(authService, times(1)).getCurrentUser(mockToken);
    }

    @Test
    void testGetCurrentUser_NoToken_ReturnsUnauthorized() throws Exception {
        // Arrange - no token provided

        // Act & Assert
        mockMvc.perform(get("/api/auth/current"))
                .andExpect(status().isUnauthorized());

        // Verify
        verify(jwtUtil, never()).validateToken(anyString());
        verify(authService, never()).getCurrentUser(anyString());
    }

    @Test
    void testGetCurrentUser_EmptyToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        String emptyToken = "";

        // Act & Assert
        mockMvc.perform(get("/api/auth/current")
                        .cookie(new Cookie("jwt", emptyToken)))
                .andExpect(status().isUnauthorized());

        // Verify
        verify(jwtUtil, never()).validateToken(anyString());
        verify(authService, never()).getCurrentUser(anyString());
    }

    @Test
    void testGetCurrentUser_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/current")
                        .cookie(new Cookie("jwt", invalidToken)))
                .andExpect(status().isUnauthorized());

        // Verify
        verify(jwtUtil, times(1)).validateToken(invalidToken);
        verify(authService, never()).getCurrentUser(anyString());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    void testLogout_Success() throws Exception {
        // Arrange - no special setup needed

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().maxAge("jwt", 0))
                .andExpect(cookie().value("jwt", ""))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().httpOnly("jwt", true))
                .andReturn();

        // Verify cookie is cleared
        Cookie jwtCookie = result.getResponse().getCookie("jwt");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEmpty();
        assertThat(jwtCookie.getMaxAge()).isZero();
    }

    @Test
    void testLogout_WithExistingCookie_ClearsIt() throws Exception {
        // Arrange
        String existingToken = "existing.jwt.token";

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("jwt", existingToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().maxAge("jwt", 0))
                .andReturn();

        // Verify
        Cookie jwtCookie = result.getResponse().getCookie("jwt");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEmpty();
        assertThat(jwtCookie.getMaxAge()).isZero();
    }
}