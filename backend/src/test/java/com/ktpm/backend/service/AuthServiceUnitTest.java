package com.ktpm.backend.service;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.entity.User;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.repository.UserRepository;
import com.ktpm.backend.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    @DisplayName("Login thành công với username và password đúng")
    void loginUserSuccess() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            validatorMock.when(() -> Validator.sanitizeInput(testUsername)).thenReturn(testUsername);
            validatorMock.when(() -> Validator.sanitizeInput(testPassword)).thenReturn(testPassword);
            when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

            // Act
            LoginResponseDTO result = authService.authenticate(testUsername, testPassword);

            // Assert
            assertNotNull(result);
            assertEquals(testUserId, result.getId());
            assertEquals(testUsername, result.getUsername());
            verify(userRepository, times(1)).findByUsername(testUsername);
            verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        }
    }

    @Test
    @DisplayName("Login thất bại - Username không tồn tại")
    void loginUserUsernameNotFound() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class, CALLS_REAL_METHODS)) {
            // Arrange
            String nonExistentUsername = "nonexistent";
            validatorMock.when(() -> Validator.isValidUsername(nonExistentUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            validatorMock.when(() -> Validator.sanitizeInput(testUsername)).thenReturn(testUsername);
            validatorMock.when(() -> Validator.sanitizeInput(testPassword)).thenReturn(testPassword);
            when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> authService.authenticate(nonExistentUsername, testPassword)
            );

            assertEquals("Không tìm thấy người dùng", exception.getMessage());
            verify(userRepository, times(1)).findByUsername(nonExistentUsername);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password sai")
    void loginUserWrongPassword() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            String wrong = "WrongPass123";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(wrong)).thenReturn(true);
            validatorMock.when(() -> Validator.sanitizeInput(testUsername)).thenReturn(testUsername);
            validatorMock.when(() -> Validator.sanitizeInput(wrong)).thenReturn(wrong);

            WrongPassWordException exception = assertThrows(
                    WrongPassWordException.class,
                    () -> authService.authenticate(testUsername, wrong)
            );

            assertEquals('Sai mâjt ')
        }
    }

















    


    @Test
    @DisplayName("Login thất bại - Username null")
    void loginUserInvalidUsernameNull() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            validatorMock.when(() -> Validator.isValidUsername(null)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(null, testPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
            verify(passwordEncoder, never()).matches(any(), any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password null")
    void loginUserInvalidPasswordNull() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(null)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(testUsername, null)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
            verify(passwordEncoder, never()).matches(any(), any());
        }
    }
}