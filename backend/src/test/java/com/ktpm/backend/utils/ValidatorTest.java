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
    @DisplayName("String null/rỗng trả về true")
    void isBlankNullOrEmptyReturnsTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", "\t\n", "  \t  \n  "})
    @DisplayName("String chỉ chứa khoảng trắng trả về true")
    void isBlankWhitespaceOnlyReturnsTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", " test ", "a", "  hello world  ", "\tvalue\n"})
    @DisplayName("String có nội dung trả về false")
    void isBlankNonBlankReturnsFalse(String str) {
        assertFalse(Validator.isBlank(str));
    }

    // ==================== isValidUsername() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "   "})
    @DisplayName("Username null/rỗng/blank trả về false")
    void isValidUsernameNullOrBlankReturnsFalse(String username) {
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
    })
    @DisplayName("Username hợp lệ")
    void isValidUsernameValidReturnsTrue(String username) {
        assertTrue(Validator.isValidUsername(username));
    }

    @Test
    @DisplayName("Username đúng 3 ký tự (boundary min)")
    void isValidUsernameMinLengthReturnsTrue() {
        String username = "abc";
        assertTrue(Validator.isValidUsername(username));
    }

    @Test
    @DisplayName("Username đúng 50 ký tự (boundary max)")
    void isValidUsernameMaxLengthReturnsTrue() {
        String username = "a".repeat(50);
        assertTrue(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",                           // Quá ngắn (< 3 ký tự)
            "a",                            // Quá ngắn (1 ký tự)
            "user@#!name",                  // Chứa ký tự không hợp lệ
            "user name",                    // Chứa khoảng trắng
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // Quá dài (> 50 ký tự)
    })
    @DisplayName("Username không hợp lệ")
    void isUsernameInvalidReturnsFalse(String username) {
        assertFalse(Validator.isValidUsername(username));
    }

    // ==================== isValidPassword() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n", "   "})
    @DisplayName("Password null/rỗng/blank trả về false")
    void isValidPasswordNullOrBlankReturnsFalse(String password) {
        assertFalse(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass123",
            "Abc123456",
            "Test1234",
            "a1b2c3d4e5"
    })
    @DisplayName("Password hợp lệ")
    void isValidPassword_Valid_ReturnsTrue(String password) {
        assertTrue(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("Password đúng 6 ký tự (boundary min)")
    void isValidPasswordMinLengthReturnsTrue() {
        String password = "Abcd12";
        assertTrue(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("Password đúng 100 ký tự (boundary max)")
    void isValidPasswordMaxLengthReturnsTrue() {
        String password = "Pass1" + "a".repeat(95);
        assertTrue(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass1",                        // Quá ngắn (< 6 ký tự)
            "password",                     // Không có số
            "abcdefgh",                     // Không có số
            "12345678",                     // Không có chữ
            "123456",                       // Không có chữ
            "Pass@#!123",                   // Chứa ký tự đặc biệt
            "Pass 123",                     // Chứa khoảng trắng
            "Pass1aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"  // Quá dài (> 100 ký tự)
    })
    @DisplayName("Password không hợp lệ")
    void isValidPasswordInvalidReturnsFalse(String password) {
        assertFalse(Validator.isValidPassword(password));
    }
}
