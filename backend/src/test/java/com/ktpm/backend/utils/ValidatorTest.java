package com.ktpm.backend.utils;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validator Unit Tests")
class ValidatorTest {

    // ==================== isBlank() TESTS ====================

    @Test
    @DisplayName("isBlank - String null trả về true")
    void isBlank_NullString_ReturnsTrue() {
        assertTrue(Validator.isBlank(null));
    }

    @Test
    @DisplayName("isBlank - String rỗng trả về true")
    void isBlank_EmptyString_ReturnsTrue() {
        assertTrue(Validator.isBlank(""));
    }

    @Test
    @DisplayName("isBlank - String chỉ chứa khoảng trắng trả về true")
    void isBlank_WhitespaceString_ReturnsTrue() {
        assertTrue(Validator.isBlank("   "));
        assertTrue(Validator.isBlank("\t\n"));
    }

    @Test
    @DisplayName("isBlank - String có nội dung trả về false")
    void isBlank_NonBlankString_ReturnsFalse() {
        assertFalse(Validator.isBlank("test"));
        assertFalse(Validator.isBlank(" test "));
    }

    // ==================== isSizeInRange() TESTS ====================

    @Test
    @DisplayName("isSizeInRange - String null trả về false")
    void isSizeInRange_NullString_ReturnsFalse() {
        assertFalse(Validator.isSizeInRange(null, 1, 10));
    }

    @Test
    @DisplayName("isSizeInRange - String trong khoảng cho phép")
    void isSizeInRange_ValidRange_ReturnsTrue() {
        assertTrue(Validator.isSizeInRange("test", 3, 10));
        assertTrue(Validator.isSizeInRange("abc", 3, 5));
        assertTrue(Validator.isSizeInRange("12345", 5, 5));
    }

    @Test
    @DisplayName("isSizeInRange - String ngắn hơn min")
    void isSizeInRange_TooShort_ReturnsFalse() {
        assertFalse(Validator.isSizeInRange("ab", 3, 10));
    }

    @Test
    @DisplayName("isSizeInRange - String dài hơn max")
    void isSizeInRange_TooLong_ReturnsFalse() {
        assertFalse(Validator.isSizeInRange("toolongstring", 3, 10));
    }

    // ==================== isValidUsername() TESTS ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
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
    void isValidUsername_ValidUsernames_ReturnsTrue(String username) {
        assertTrue(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",                           // Quá ngắn (< 3 ký tự)
            "user@name",                    // Chứa ký tự không hợp lệ (@)
            "user name",                    // Chứa khoảng trắng
            "user#123",                     // Chứa ký tự đặc biệt (#)
            "user!",                        // Chứa ký tự đặc biệt (!)
            "user$123",                     // Chứa ký tự đặc biệt ($)
    })
    @DisplayName("isValidUsername - Username không hợp lệ")
    void isValidUsername_InvalidUsernames_ReturnsFalse(String username) {
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
    @ValueSource(strings = {"  ", "\t"})
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
    void isValidPassword_ValidPasswords_ReturnsTrue(String password) {
        assertTrue(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass1",                        // Quá ngắn (< 6 ký tự)
            "password",                     // Không có số
            "12345678",                     // Không có chữ
            "Pass@123",                     // Chứa ký tự đặc biệt
            "Pass 123",                     // Chứa khoảng trắng
    })
    @DisplayName("isValidPassword - Password không hợp lệ")
    void isValidPassword_InvalidPasswords_ReturnsFalse(String password) {
        assertFalse(Validator.isValidPassword(password));
    }

    @Test
    @DisplayName("isValidPassword - Password quá dài (> 100 ký tự)")
    void isValidPassword_TooLong_ReturnsFalse() {
        String longPassword = "Pass1" + "a".repeat(96);
        assertFalse(Validator.isValidPassword(longPassword));
    }

//    // ==================== isValidProduct() TESTS ====================
//
//    @Test
//    @DisplayName("isValidProduct - Product null trả về false")
//    void isValidProduct_NullProduct_ReturnsFalse() {
//        assertFalse(Validator.isValidProduct(null));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Product hợp lệ đầy đủ")
//    void isValidProduct_ValidProduct_ReturnsTrue() {
//        Product product = new Product();
//        product.setProductName("Test Product");
//        product.setPrice(99);
//        product.setQuantity(10);
//        product.setDescription("This is a test product description");
//        product.setCategory(Category.);
//
//        assertTrue(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Product name null/blank")
//    void isValidProduct_InvalidProductName_ReturnsFalse() {
//        Product product = createValidProduct();
//        product.setProductName(null);
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setProductName("");
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setProductName("ab"); // < 3 ký tự
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Product name quá dài")
//    void isValidProduct_ProductNameTooLong_ReturnsFalse() {
//        Product product = createValidProduct();
//        product.setProductName("a".repeat(101)); // > 100 ký tự
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Price không hợp lệ")
//    void isValidProduct_InvalidPrice_ReturnsFalse() {
//        Product product = createValidProduct();
//
//        product.setPrice(null);
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setPrice(0); // < 0.01
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setPrice(-10); // Âm
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setPrice(1000000000); // > 999999999
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Quantity không hợp lệ")
//    void isValidProduct_InvalidQuantity_ReturnsFalse() {
//        Product product = createValidProduct();
//
//        product.setQuantity(null);
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setQuantity(-1); // Âm
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setQuantity(100000); // > 99999
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Description không hợp lệ")
//    void isValidProduct_InvalidDescription_ReturnsFalse() {
//        Product product = createValidProduct();
//
//        product.setDescription(null);
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setDescription("");
//        assertFalse(Validator.isValidProduct(product));
//
//        product.setDescription("a".repeat(501)); // > 500 ký tự
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    @Test
//    @DisplayName("isValidProduct - Category không hợp lệ")
//    void isValidProduct_InvalidCategory_ReturnsFalse() {
//        Product product = createValidProduct();
//        product.setCategory(null);
//        assertFalse(Validator.isValidProduct(product));
//    }
//
//    // ==================== isValidCategory() TESTS ====================
//
//    @Test
//    @DisplayName("isValidCategory - Category hợp lệ")
//    void isValidCategory_ValidCategories_ReturnsTrue() {
//        // Giả sử Category enum có các giá trị: ELECTRONICS, CLOTHING, FOOD
//        assertTrue(Validator.isValidCategory("ELECTRONICS"));
//        assertTrue(Validator.isValidCategory("electronics")); // Case insensitive
//        assertTrue(Validator.isValidCategory("Electronics"));
//    }
//
//    @Test
//    @DisplayName("isValidCategory - Category không hợp lệ")
//    void isValidCategory_InvalidCategory_ReturnsFalse() {
//        assertFalse(Validator.isValidCategory("INVALID_CATEGORY"));
//        assertFalse(Validator.isValidCategory(""));
//        assertFalse(Validator.isValidCategory(null));
//    }
//
//    // ==================== HELPER METHODS ====================
//
//    private Product createValidProduct() {
//        Product product = new Product();
//        product.setProductName("Test Product");
//        product.setPrice(99);
//        product.setQuantity(10);
//        product.setDescription("Valid description");
//        product.setCategory(Category.ELECTRONICS);
//        return product;
//    }
}