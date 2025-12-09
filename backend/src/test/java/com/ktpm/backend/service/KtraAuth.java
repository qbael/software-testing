package com.ktpm.backend.service;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.entity.User;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.repository.UserRepository;
import com.ktpm.backend.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KtraAuth {
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
    void loginUserUsernameNotFound() {
        try(MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            String nonUser = "nonUser";
            validatorMock.when(() -> Validator.isValidUsername(nonUser)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            validatorMock.when(() -> Validator.sanitizeInput(nonUser)).thenReturn(nonUser);
            validatorMock.when(() -> Validator.sanitizeInput(testPassword)).thenReturn(testPassword);
            when(userRepository.findByUsername(nonUser)).thenReturn(Optional.empty());

            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> authService.authenticate(nonUser, testPassword)
            );

            assertEquals("Không tìm thấy người dùng", exception.getMessage());
            verify(userRepository, times(1)).findByUsername(nonUser);
            verify(passwordEncoder, never()).matches(testPassword, encodedPassword);
        }
    }

    @Test
    void loginUserWrongPassword() {
        try(MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            String wrongPassword = "WrongPass123";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(wrongPassword)).thenReturn(true);
            validatorMock.when(() -> Validator.sanitizeInput(testUsername)).thenReturn(testUsername);
            validatorMock.when(() -> Validator.sanitizeInput(wrongPassword)).thenReturn(wrongPassword);
            when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

            WrongPassWordException exception = assertThrows(
                    WrongPassWordException.class,
                    () -> authService.authenticate(testUsername, wrongPassword)
            );

            assertEquals("Sai mật khẩu", exception.getMessage());
            verify(userRepository, times(1)).findByUsername(testUsername);
            verify(passwordEncoder, times(1)).matches(wrongPassword, encodedPassword);
        }
    }

    @Test
    void loginUserUsernameNull() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            validatorMock.when(() -> Validator.isValidUsername(null)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(null, testPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
            verify(passwordEncoder, never()).matches(any(), any());
        }
    }
}
