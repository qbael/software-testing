package com.ktpm.backend.controller;

import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private UUID testUserId;
    private String testUsername;
    private String testPassword;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
        testPassword = "testpassword";
        testToken = "mock.jwt.token";
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(testUsername, testPassword);
        LoginResponseDTO loginResponse = new LoginResponseDTO(testUserId, testUsername);

        when(authService.loginUser(testUsername, testPassword)).thenReturn(loginResponse);
        when(jwtUtil.generateToken(testUserId, testUsername)).thenReturn(testToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(testUserId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(testUsername))
                .andExpect(cookie().value("jwt", testToken))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().secure("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().maxAge("jwt", 24 * 60 * 60 * 3));

        verify(authService).loginUser(testUsername, testPassword);
        verify(jwtUtil).generateToken(testUserId, testUsername);
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO(testUsername, testPassword);

        doThrow(UsernameNotFoundException.class).when(authService).loginUser(testUsername, testPassword);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        verify(authService).loginUser(testUsername, testPassword);
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalid@username\",\"password\":\"short\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(authService, Mockito.times(0)).loginUser(anyString(), anyString());
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(testUsername, testPassword, testPassword);

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\",\"verifyPassword\":\"" + testPassword + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_UsernameExists() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(testUsername, testPassword, testPassword);

        doThrow(UsernameExistedException.class).when(authService).register(any(RegisterRequestDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\",\"verifyPassword\":\"" + testPassword + "\"}"))
                .andExpect(MockMvcResultMatchers.status().isConflict());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_PasswordMismatch() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(testUsername, testPassword, "different");

        doThrow(VerifyPasswordNotMatch.class).when(authService).register(any(RegisterRequestDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\",\"verifyPassword\":\"different\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\",\"verifyPassword\":\"\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(authService, Mockito.times(0)).register(any(RegisterRequestDTO.class));
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        LoginResponseDTO loginResponse = new LoginResponseDTO(testUserId, testUsername);

        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(authService.getCurrentUser(testToken)).thenReturn(loginResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/current")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", testToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(testUserId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(testUsername));

        verify(jwtUtil).validateToken(testToken);
        verify(authService).getCurrentUser(testToken);
    }

    @Test
    void testGetCurrentUser_NoToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/current"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        verify(jwtUtil, Mockito.times(0)).validateToken(anyString());
        verify(authService, Mockito.times(0)).getCurrentUser(anyString());
    }

    @Test
    void testGetCurrentUser_InvalidToken() throws Exception {
        when(jwtUtil.validateToken(testToken)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/current")
                        .cookie(new jakarta.servlet.http.Cookie("jwt", testToken)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        verify(jwtUtil).validateToken(testToken);
        verify(authService, Mockito.times(0)).getCurrentUser(anyString());
    }

    @Test
    void testLogout_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(cookie().value("jwt", ""))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().path("jwt", "/"))
                .andExpect(cookie().maxAge("jwt", 0));

        // No mock interactions to verify for logout as it doesn't call service
    }
}