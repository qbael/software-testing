package com.ktpm.backend.service;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.User;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.repository.UserRepository;
import com.ktpm.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID testUserId;
    private String testUsername;
    private String testPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "testuser";
        testPassword = "Password123";
        encodedPassword = "$2a$10$encodedPasswordHash";

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername(testUsername);
        testUser.setPassword(encodedPassword);
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login thành công với username và password đúng")
    void loginUser_Success() {
        // Arrange
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

        // Act
        LoginResponseDTO result = authService.loginUser(testUsername, testPassword);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals(testUsername, result.getUsername());
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
    }

    @Test
    @DisplayName("Login thất bại - Username không tồn tại")
    void loginUser_UsernameNotFound() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authService.loginUser(nonExistentUsername, testPassword)
        );

        assertEquals("Không tìm thấy người dùng", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Login thất bại - Password sai")
    void loginUser_WrongPassword() {
        // Arrange
        String wrongPassword = "WrongPassword123";
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        // Act & Assert
        WrongPassWordException exception = assertThrows(
                WrongPassWordException.class,
                () -> authService.loginUser(testUsername, wrongPassword)
        );

        assertEquals("Sai mật khẩu", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(passwordEncoder, times(1)).matches(wrongPassword, encodedPassword);
    }

    @Test
    @DisplayName("Login với username null")
    void loginUser_NullUsername() {
        // Arrange
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authService.loginUser(null, testPassword));
        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    @DisplayName("Login với password null")
    void loginUser_NullPassword() {
        // Arrange
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(null, encodedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(WrongPassWordException.class, () -> authService.loginUser(testUsername, null));
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(passwordEncoder, times(1)).matches(null, encodedPassword);
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register thành công")
    void register_Success() {
        // Arrange
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("Password123");
        registerDTO.setVerifyPassword("Password123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = authService.register(registerDTO);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("Password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register thất bại - Username đã tồn tại")
    void register_UsernameExists() {
        // Arrange
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername(testUsername);
        registerDTO.setPassword("Password123");
        registerDTO.setVerifyPassword("Password123");

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        // Act & Assert
        UsernameExistedException exception = assertThrows(
                UsernameExistedException.class,
                () -> authService.register(registerDTO)
        );

        assertEquals("Tên đăng nhập đã tồn tại", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register thất bại - Password và VerifyPassword không khớp")
    void register_PasswordMismatch() {
        // Arrange
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("Password123");
        registerDTO.setVerifyPassword("DifferentPassword123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        // Act & Assert
        VerifyPasswordNotMatch exception = assertThrows(
                VerifyPasswordNotMatch.class,
                () -> authService.register(registerDTO)
        );

        assertEquals("Mật khẩu xác nhận không khớp", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register thất bại - Database error")
    void register_DatabaseError() {
        // Arrange
        RegisterRequestDTO registerDTO = new RegisterRequestDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("Password123");
        registerDTO.setVerifyPassword("Password123");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = authService.register(registerDTO);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ==================== GET CURRENT USER TESTS ====================

    @Test
    @DisplayName("Get current user thành công")
    void getCurrentUser_Success() {
        // Arrange
        String token = "valid.jwt.token";
        when(jwtUtil.extractId(token)).thenReturn(testUserId);
        when(jwtUtil.extractUsername(token)).thenReturn(testUsername);

        // Act
        LoginResponseDTO result = authService.getCurrentUser(token);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals(testUsername, result.getUsername());
        verify(jwtUtil, times(1)).extractId(token);
        verify(jwtUtil, times(1)).extractUsername(token);
    }

    @Test
    @DisplayName("Get current user với token null")
    void getCurrentUser_NullToken() {
        // Act
        LoginResponseDTO result = authService.getCurrentUser(null);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUsername());
        verify(jwtUtil, times(1)).extractId(null);
        verify(jwtUtil, times(1)).extractUsername(null);
    }

    @Test
    @DisplayName("Get current user với token rỗng")
    void getCurrentUser_EmptyToken() {
        // Arrange
        String emptyToken = "";
        when(jwtUtil.extractId(emptyToken)).thenReturn(null);
        when(jwtUtil.extractUsername(emptyToken)).thenReturn(null);

        // Act
        LoginResponseDTO result = authService.getCurrentUser(emptyToken);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getUsername());
    }
}