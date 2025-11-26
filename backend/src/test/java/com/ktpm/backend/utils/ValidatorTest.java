package com.ktpm.backend.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validator Unit Tests")
class ValidatorTest {

    // ==================== isBlank() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("isBlank - String null/rỗng trả về true")
    void isBlank_NullOrEmpty_ReturnsTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", "\t\n", "  \t  \n  "})
    @DisplayName("isBlank - String chỉ chứa khoảng trắng trả về true")
    void isBlank_WhitespaceOnly_ReturnsTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", " test ", "a", "  hello world  ", "\tvalue\n"})
    @DisplayName("isBlank - String có nội dung trả về false")
    void isBlank_NonBlank_ReturnsFalse(String str) {
        assertFalse(Validator.isBlank(str));
    }

    // ==================== isValidUsername() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "   "})
    @DisplayName("isValidUsername - Username null/rỗng/blank trả về false")
    void isValidUsername_NullOrBlank_ReturnsFalse(String username) {
        assertFalse(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user123",
            "test_user",
            "john.doe",
            "user-name",
            "a1b2c3",
            "ABC",
            "user_123.test-name"
    })
    @DisplayName("isValidUsername - Username hợp lệ")
    void isValidUsername_Valid_ReturnsTrue(String username) {
        assertTrue(Validator.isValidUsername(username));
    }

    @Test
    @DisplayName("isValidUsername - Username đúng 3 ký tự (boundary min)")
    void isValidUsername_MinLength_ReturnsTrue() {
        String username = "abc";
        assertTrue(Validator.isValidUsername(username));
    }

    @Test
    @DisplayName("isValidUsername - Username đúng 50 ký tự (boundary max)")
    void isValidUsername_MaxLength_ReturnsTrue() {
        String username = "a".repeat(50);
        assertTrue(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",                           // Quá ngắn (< 3 ký tự)
            "a",                            // Quá ngắn (1 ký tự)
            "user@name",                    // Chứa ký tự không hợp lệ (@)
            "user name",                    // Chứa khoảng trắng
            "user#123",                     // Chứa ký tự đặc biệt (#)
            "user!",                        // Chứa ký tự đặc biệt (!)
            "user$123",                     // Chứa ký tự đặc biệt ($)
            "user%test",                    // Chứa ký tự đặc biệt (%)
            "user&test",                    // Chứa ký tự đặc biệt (&)
    })
    @DisplayName("isValidUsername - Username không hợp lệ")
    void isValidUsername_Invalid_ReturnsFalse(String username) {
        assertFalse(Validator.isValidUsername(username));
    }

    @Test
    @DisplayName("isValidUsername - Username quá dài (> 50 ký tự)")
    void isValidUsername_TooLong_ReturnsFalse() {
        String longUsername = "a".repeat(51);
        assertFalse(Validator.isValidUsername(longUsername));
    }

    // ==================== isValidPassword() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "   "})
    @DisplayName("isValidPassword - Password null/rỗng/blank trả về false")
    void isValidPassword_NullOrBlank_ReturnsFalse(String password) {
        assertFalse(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass123",
            "Password1",
            "Abc123456",
            "Test1234",
            "a1b2c3d4e5"
    })
    @DisplayName("isValidPassword - Password hợp lệ")
    void isValidPassword_Valid_ReturnsTrue(String password) {
        assertTrue(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("isValidPassword - Password đúng 6 ký tự (boundary min)")
    void isValidPassword_MinLength_ReturnsTrue() {
        String password = "Abcd12";
        assertTrue(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("isValidPassword - Password đúng 100 ký tự (boundary max)")
    void isValidPassword_MaxLength_ReturnsTrue() {
        String password = "Pass1" + "a".repeat(95);
        assertTrue(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass1",                        // Quá ngắn (< 6 ký tự)
            "Abc1",                         // Quá ngắn (4 ký tự)
            "a1",                           // Quá ngắn (2 ký tự)
            "password",                     // Không có số
            "abcdefgh",                     // Không có số
            "12345678",                     // Không có chữ
            "123456",                       // Không có chữ
            "Pass@123",                     // Chứa ký tự đặc biệt (@)
            "Pass 123",                     // Chứa khoảng trắng
            "Pass#123",                     // Chứa ký tự đặc biệt (#)
            "Pass!123",                     // Chứa ký tự đặc biệt (!)
            "Pass$123",                     // Chứa ký tự đặc biệt ($)
    })
    @DisplayName("isValidPassword - Password không hợp lệ")
    void isValidPassword_Invalid_ReturnsFalse(String password) {
        assertFalse(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("isValidPassword - Password quá dài (> 100 ký tự)")
    void isValidPassword_TooLong_ReturnsFalse() {
        String longPassword = "Pass1" + "a".repeat(96);
        assertFalse(Validator.isValidPassword(longPassword));
    }
}
