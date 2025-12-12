package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.utils.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Test - Input Validation & Sanitization")
public class InputValidationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "user@name",
            "user name",
            "user#123",
            "user!",
            "user$",
            "user%",
            "user&test",
            "user*",
            "user+test",
            "user=test",
            "user[test]",
            "user{test}",
            "user|test",
            "user\\test",
            "user:test",
            "user;test",
            "user'test",
            "user\"test",
            "user<>test",
            "user,test",
            "user?test"
    })
    @DisplayName("Input Validation Test 1: Username với ký tự đặc biệt nên bị từ chối")
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

    @ParameterizedTest
    @ValueSource(strings = {
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "onload=alert('xss')",
            "onerror=alert('xss')",
            "onclick=alert('xss')",
            "<iframe src='malicious.com'>",
            "eval('malicious code')",
            "alert('xss')",
            "document.cookie"
    })
    @DisplayName("Input Validation Test 2: Các input chứa XSS nên bị phát hiện")
    void containsXSS_ShouldDetectXSSPatterns(String input) {
        assertTrue(Validator.containsXSS(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "SELECT * FROM users",
            "INSERT INTO users",
            "UPDATE users SET",
            "DELETE FROM users",
            "UNION SELECT",
            "admin'--",
            "'; EXEC xp_cmdshell('dir'); --"
    })
    @DisplayName("Input Validation Test 3: Các input chứa SQL Injection nên bị phát hiện")
    void containsSqlInjection_ShouldDetectSQLInjectionPatterns(String input) {
        assertTrue(Validator.containsSqlInjection(input));
    }


    @Test
    @DisplayName("Input Sanitization Test 1: Các input chứa SQL Injection và XSS nên được làm sạch")
    void sanitizeInput_ShouldRemoveXSSAndSQLInjectionPatterns() {
        String maliciousInput = "<script>alert('xss')</script>'; DROP TABLE users; --";
        String sanitized = Validator.sanitizeInput(maliciousInput);

        assertThat(sanitized)
                .doesNotContain("<script>")
                .doesNotContain("</script>")
                .doesNotContain("DROP TABLE")
                .doesNotContain("';");
    }

    @Test
    @DisplayName("Input Sanitization Test 2: Các ký tự đặc biệt trong HTML nên được mã hóa")
    void sanitizeInput_ShouldEncodeHTMLSpecialCharacters() {
        String input = "<div>\"test\" & 'value'</div>";
        String sanitized = Validator.sanitizeInput(input);

        assertThat(sanitized).contains("&quot;test&quot; &amp;");
    }

    @Test
    @DisplayName("Input Sanitization Test 3: sanitizeProduct nên làm sạch tất cả các trường chuỗi")
    void sanitizeProduct_ShouldSanitizeAllStringFields() {
        Product maliciousProduct = new Product();
        maliciousProduct.setId(UUID.randomUUID());
        maliciousProduct.setProductName("<script>alert('xss')</script>");
        maliciousProduct.setPrice(100000);
        maliciousProduct.setQuantity(10);
        maliciousProduct.setDescription("'; DROP TABLE products; --");
        maliciousProduct.setCategory(Category.SMARTPHONE);

        Product sanitized = Validator.sanitizeProduct(maliciousProduct);

        assertThat(sanitized.getProductName())
                .doesNotContain("<script>")
                .doesNotContain("</script>");

        assertThat(sanitized.getDescription())
                .doesNotContain("DROP TABLE")
                .doesNotContain("';");

        assertEquals(maliciousProduct.getPrice(), sanitized.getPrice());
        assertEquals(maliciousProduct.getQuantity(), sanitized.getQuantity());
        assertEquals(maliciousProduct.getCategory(), sanitized.getCategory());
    }
}