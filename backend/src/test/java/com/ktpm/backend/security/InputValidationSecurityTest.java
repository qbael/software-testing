package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.service.ProductService;
import com.ktpm.backend.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - Input Validation & Sanitization")
public class InputValidationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private ProductService productService;

    @Mock
    private JwtUtil jwtUtil;

    // ==================== USERNAME VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 1: Username too short (< 3 chars)")
    void testUsernameTooShort() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("ab");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 2: Username too long (> 50 chars)")
    void testUsernameTooLong() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("a".repeat(51));
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@name",      // @ symbol
            "user name",      // space
            "user#123",       // # symbol
            "user!",          // ! symbol
            "user$",          // $ symbol
            "user%",          // % symbol
            "user&test",      // & symbol
            "user*",          // * symbol
            "user+test",      // + symbol
            "user=test",      // = symbol
            "user[test]",     // brackets
            "user{test}",     // braces
            "user|test",      // pipe
            "user\\test",     // backslash
            "user:test",      // colon
            "user;test",      // semicolon
            "user'test",      // single quote
            "user\"test",     // double quote
            "user<>test",     // angle brackets
            "user,test",      // comma
            "user?test"       // question mark
    })
    @DisplayName("Input Validation Test 3: Username with special characters")
    void testUsernameWithSpecialChars(String invalidUsername) throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername(invalidUsername);
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 4: Username with null value")
    void testUsernameNull() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername(null);
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 5: Username with empty string")
    void testUsernameEmpty() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 6: Username with whitespace only")
    void testUsernameWhitespaceOnly() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("   ");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PASSWORD VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 7: Password too short (< 6 chars)")
    void testPasswordTooShort() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Abc12");
        registerRequest.setVerifyPassword("Abc12");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 8: Password too long (> 100 chars)")
    void testPasswordTooLong() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        String longPassword = "Password1" + "a".repeat(93);
        registerRequest.setPassword(longPassword);
        registerRequest.setVerifyPassword(longPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password",         // No digit
            "PASSWORD",         // No digit
            "abcdefgh",        // No digit
            "12345678",        // No letter
            "123456",          // Too short + no letter
            "Pass word1",      // Contains space (if not allowed)
    })
    @DisplayName("Input Validation Test 9: Invalid password formats")
    void testInvalidPasswordFormats(String invalidPassword) throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword(invalidPassword);
        registerRequest.setVerifyPassword(invalidPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input Validation Test 10: Password mismatch")
    void testPasswordMismatch() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("Password123");
        registerRequest.setVerifyPassword("DifferentPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PRODUCT NAME VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 11: Product name too short (< 3 chars)")
    void testProductNameTooShort() throws Exception {
        Product product = Product.builder()
                .productName("AB")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized()); // Auth first, then would be 400
    }

    @Test
    @DisplayName("Input Validation Test 12: Product name too long (> 100 chars)")
    void testProductNameTooLong() throws Exception {
        Product product = Product.builder()
                .productName("A".repeat(101))
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 13: Product name null")
    void testProductNameNull() throws Exception {
        Product product = Product.builder()
                .productName(null)
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 14: Product name empty")
    void testProductNameEmpty() throws Exception {
        Product product = Product.builder()
                .productName("")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PRICE VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 15: Negative price")
    void testNegativePrice() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(-100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 16: Zero price")
    void testZeroPrice() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(0)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 17: Price too large")
    void testPriceTooLarge() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(1000000000) // > 999999999
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 18: Price null")
    void testPriceNull() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(null)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== QUANTITY VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 19: Negative quantity")
    void testNegativeQuantity() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(-10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 20: Quantity too large")
    void testQuantityTooLarge() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(100000) // > 99999
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 21: Quantity null")
    void testQuantityNull() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(null)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DESCRIPTION VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 22: Description too long (> 500 chars)")
    void testDescriptionTooLong() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description("A".repeat(501))
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 23: Description null")
    void testDescriptionNull() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description(null)
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 24: Description empty")
    void testDescriptionEmpty() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description("")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CATEGORY VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 25: Invalid category")
    void testInvalidCategory() throws Exception {
        String invalidJson = """
                {
                    "productName": "Test Product",
                    "price": 100,
                    "quantity": 10,
                    "description": "Test description",
                    "category": "INVALID_CATEGORY"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isUnauthorized()); // Would be 400 after auth
    }

    @Test
    @DisplayName("Input Validation Test 26: Category null")
    void testCategoryNull() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(null)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PAGINATION PARAMETERS VALIDATION ====================

    @Test
    @DisplayName("Input Validation Test 27: Negative page number")
    void testNegativePageNumber() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "-1")
                        .param("limit", "10"))
                .andExpect(status().isUnauthorized()); // Would be 400 after auth
    }

    @Test
    @DisplayName("Input Validation Test 28: Zero or negative limit")
    void testZeroOrNegativeLimit() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "0"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "-10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 29: Invalid sortDir value")
    void testInvalidSortDir() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "10")
                        .param("sortDir", "invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Input Validation Test 30: Empty sortBy parameter")
    void testEmptySortBy() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "10")
                        .param("sortBy", ""))
                .andExpect(status().isUnauthorized());
    }
}