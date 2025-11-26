package com.ktpm.backend.service;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.entity.User;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.repository.UserRepository;
import com.ktpm.backend.utils.JwtUtil;
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
class AuthServiceTest {

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
    void loginUser_Success() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
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
    void loginUser_UsernameNotFound() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String nonExistentUsername = "nonexistent";
            validatorMock.when(() -> Validator.isValidUsername(nonExistentUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
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
    void loginUser_WrongPassword() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String wrongPassword = "WrongPassword123";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(wrongPassword)).thenReturn(true);
            when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

            // Act & Assert
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
    @DisplayName("Login thất bại - Username null")
    void loginUser_InvalidUsernameNull() {
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
    @DisplayName("Login thất bại - Username rỗng")
    void loginUser_InvalidUsernameBlank() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String blankUsername = "";
            validatorMock.when(() -> Validator.isValidUsername(blankUsername)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(blankUsername, testPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Username quá ngắn")
    void loginUser_InvalidUsernameTooShort() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String shortUsername = "ab";
            validatorMock.when(() -> Validator.isValidUsername(shortUsername)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(shortUsername, testPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Username chứa ký tự đặc biệt không hợp lệ")
    void loginUser_InvalidUsernameSpecialChars() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String invalidUsername = "user@name";
            validatorMock.when(() -> Validator.isValidUsername(invalidUsername)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(invalidUsername, testPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password null")
    void loginUser_InvalidPasswordNull() {
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
        }
    }

    @Test
    @DisplayName("Login thất bại - Password rỗng")
    void loginUser_InvalidPasswordBlank() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String blankPassword = "";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(blankPassword)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(testUsername, blankPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password quá ngắn")
    void loginUser_InvalidPasswordTooShort() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String shortPassword = "Pass1";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(shortPassword)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(testUsername, shortPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password thiếu chữ số")
    void loginUser_InvalidPasswordNoDigit() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String passwordNoDigit = "Password";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(passwordNoDigit)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(testUsername, passwordNoDigit)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thất bại - Password thiếu chữ cái")
    void loginUser_InvalidPasswordNoLetter() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String passwordNoLetter = "123456";
            validatorMock.when(() -> Validator.isValidUsername(testUsername)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(passwordNoLetter)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(testUsername, passwordNoLetter)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }

    @Test
    @DisplayName("Login thành công với username chứa dấu chấm")
    void loginUser_SuccessWithDot() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String usernameWithDot = "test.user";
            testUser.setUsername(usernameWithDot);
            validatorMock.when(() -> Validator.isValidUsername(usernameWithDot)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            when(userRepository.findByUsername(usernameWithDot)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

            // Act
            LoginResponseDTO result = authService.authenticate(usernameWithDot, testPassword);

            // Assert
            assertNotNull(result);
            assertEquals(usernameWithDot, result.getUsername());
            verify(userRepository, times(1)).findByUsername(usernameWithDot);
            verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        }
    }

    @Test
    @DisplayName("Login thành công với username chứa dấu gạch dưới")
    void loginUser_SuccessWithUnderscore() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String usernameWithUnderscore = "test_user";
            testUser.setUsername(usernameWithUnderscore);
            validatorMock.when(() -> Validator.isValidUsername(usernameWithUnderscore)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            when(userRepository.findByUsername(usernameWithUnderscore)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

            // Act
            LoginResponseDTO result = authService.authenticate(usernameWithUnderscore, testPassword);

            // Assert
            assertNotNull(result);
            assertEquals(usernameWithUnderscore, result.getUsername());
            verify(userRepository, times(1)).findByUsername(usernameWithUnderscore);
            verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        }
    }

    @Test
    @DisplayName("Login thành công với username chứa dấu gạch ngang")
    void loginUser_SuccessWithHyphen() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String usernameWithHyphen = "test-user";
            testUser.setUsername(usernameWithHyphen);
            validatorMock.when(() -> Validator.isValidUsername(usernameWithHyphen)).thenReturn(true);
            validatorMock.when(() -> Validator.isValidPassword(testPassword)).thenReturn(true);
            when(userRepository.findByUsername(usernameWithHyphen)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);

            // Act
            LoginResponseDTO result = authService.authenticate(usernameWithHyphen, testPassword);

            // Assert
            assertNotNull(result);
            assertEquals(usernameWithHyphen, result.getUsername());
            verify(userRepository, times(1)).findByUsername(usernameWithHyphen);
            verify(passwordEncoder, times(1)).matches(testPassword, encodedPassword);
        }
    }

    @Test
    @DisplayName("Login thất bại - Cả username và password đều không hợp lệ")
    void loginUser_BothInvalid() {
        try (MockedStatic<Validator> validatorMock = mockStatic(Validator.class)) {
            // Arrange
            String invalidUsername = "ab";
            String invalidPassword = "123";
            validatorMock.when(() -> Validator.isValidUsername(invalidUsername)).thenReturn(false);
            validatorMock.when(() -> Validator.isValidPassword(invalidPassword)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.authenticate(invalidUsername, invalidPassword)
            );

            assertEquals("Username hoặc mật khẩu không hợp lệ", exception.getMessage());
            verify(userRepository, never()).findByUsername(any());
        }
    }
}