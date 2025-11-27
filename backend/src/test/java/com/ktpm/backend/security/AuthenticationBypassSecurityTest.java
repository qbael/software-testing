package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.service.ProductService;
import com.ktpm.backend.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - Authentication Bypass Prevention")
public class AuthenticationBypassSecurityTest {

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

    // ==================== DIRECT ACCESS ATTEMPTS ====================

    @Test
    @DisplayName("Auth Bypass Test 1: Access protected endpoint without token")
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or missing token"));
    }

    @Test
    @DisplayName("Auth Bypass Test 2: Access with empty token")
    void testAccessWithEmptyToken() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 3: Access with null token")
    void testAccessWithNullToken() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", "null")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 4: Access with malformed token")
    void testAccessWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", "invalid.token.here")))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TOKEN MANIPULATION ATTEMPTS ====================

    @Test
    @DisplayName("Auth Bypass Test 5: Tampered JWT signature")
    void testTamperedJwtSignature() throws Exception {
        // Valid structure but invalid signature
        String tamperedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.TAMPERED_SIGNATURE";

        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", tamperedToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 6: JWT with 'none' algorithm")
    void testJwtWithNoneAlgorithm() throws Exception {
        // JWT với algorithm = none (known vulnerability)
        String noneAlgoToken = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.";

        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", noneAlgoToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 7: Expired JWT token")
    void testExpiredJwtToken() throws Exception {
        // Token đã hết hạn
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 8: Token for different user")
    void testTokenForDifferentUser() throws Exception {
        // Token của user khác
        String otherUserToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Im90aGVyLXVzZXItaWQiLCJ1c2VybmFtZSI6Im90aGVydXNlciJ9.signature";

        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", otherUserToken)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CREDENTIAL STUFFING / BRUTE FORCE ====================

    @Test
    @DisplayName("Auth Bypass Test 9: Login with common passwords")
    void testLoginWithCommonPasswords() throws Exception {
        String[] commonPasswords = {
                "password", "123456", "12345678", "admin", "letmein"
        };

        for (String password : commonPasswords) {
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername("admin");
            loginRequest.setPassword(password);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isNotFound()); // User not found or invalid
        }
    }

    @Test
    @DisplayName("Auth Bypass Test 10: Login with default credentials")
    void testLoginWithDefaultCredentials() throws Exception {
        String[][] defaultCreds = {
                {"admin", "admin"},
                {"root", "root"},
                {"administrator", "administrator"},
                {"test", "test"}
        };

        for (String[] cred : defaultCreds) {
            LoginRequestDTO loginRequest = new LoginRequestDTO();
            loginRequest.setUsername(cred[0]);
            loginRequest.setPassword(cred[1]);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest()); // Invalid credentials
        }
    }

    // ==================== PATH TRAVERSAL IN AUTH ====================

    @Test
    @DisplayName("Auth Bypass Test 11: Path traversal in auth endpoints")
    void testPathTraversalInAuthEndpoints() throws Exception {
        // Thử bypass auth bằng path traversal
        mockMvc.perform(get("/api/auth/../products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 12: Double slash in URL")
    void testDoubleSlashInUrl() throws Exception {
        mockMvc.perform(get("/api//products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 13: URL encoding bypass attempt")
    void testUrlEncodingBypass() throws Exception {
        mockMvc.perform(get("/api/%70roducts")) // %70 = 'p'
                .andExpect(status().isUnauthorized());
    }

    // ==================== HEADER MANIPULATION ====================

    @Test
    @DisplayName("Auth Bypass Test 14: X-Forwarded-For header manipulation")
    void testXForwardedForManipulation() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 15: Custom authentication headers")
    void testCustomAuthHeaders() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer fake-token")
                        .header("X-API-Key", "fake-key")
                        .header("X-Auth-Token", "fake-auth"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 16: Host header injection")
    void testHostHeaderInjection() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Host", "evil.com"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== METHOD OVERRIDE ATTACKS ====================

    @Test
    @DisplayName("Auth Bypass Test 17: HTTP method override")
    void testHttpMethodOverride() throws Exception {
        // Thử override POST thành GET để bypass
        mockMvc.perform(post("/api/products")
                        .header("X-HTTP-Method-Override", "GET"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 18: Different HTTP methods on same endpoint")
    void testDifferentHttpMethods() throws Exception {
        // TRACE method
        mockMvc.perform(request(HttpMethod.valueOf("TRACE"), "/api/products"))
                .andExpect(status().isMethodNotAllowed());

        // OPTIONS on auth endpoint
        mockMvc.perform(options("/api/auth/login"))
                .andExpect(status().isOk()); // Allowed for CORS
    }

    // ==================== SESSION FIXATION ====================

    @Test
    @DisplayName("Auth Bypass Test 19: Session fixation attempt")
    void testSessionFixationAttempt() throws Exception {
        // Cố gắng fix session ID
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("JSESSIONID", "fixed-session-id"))
                        .content("{\"username\":\"testuser\",\"password\":\"password123\"}"))
                .andExpect(status().isNotFound());
    }

    // ==================== AUTHENTICATION STATE MANIPULATION ====================

    @Test
    @DisplayName("Auth Bypass Test 20: Cookie injection")
    void testCookieInjection() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", "fake-token"))
                        .cookie(new Cookie("admin", "true"))
                        .cookie(new Cookie("authenticated", "true")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 21: Multiple JWT cookies")
    void testMultipleJwtCookies() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", "fake-token-1"))
                        .cookie(new Cookie("jwt", "fake-token-2")))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TIMING ATTACK PREVENTION ====================

    @Test
    @DisplayName("Auth Bypass Test 22: Timing attack on login")
    void testTimingAttackOnLogin() throws Exception {
        LoginRequestDTO loginRequest1 = new LoginRequestDTO();
        loginRequest1.setUsername("nonexistent");
        loginRequest1.setPassword("password123");

        long start1 = System.nanoTime();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isNotFound());
        long time1 = System.nanoTime() - start1;

        LoginRequestDTO loginRequest2 = new LoginRequestDTO();
        loginRequest2.setUsername("mindang12");
        loginRequest2.setPassword("wrongpassword");

        long start2 = System.nanoTime();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest2)))
                .andExpect(status().isUnauthorized());
        long time2 = System.nanoTime() - start2;

        // Response times should be similar to prevent timing attacks
        // Allow 50% difference
        double ratio = (double) Math.max(time1, time2) / Math.min(time1, time2);
        assert ratio < 2.0 : "Potential timing attack vulnerability: response times vary too much";
    }

    // ==================== PRIVILEGE ESCALATION ====================

    @Test
    @DisplayName("Auth Bypass Test 23: Attempt to access admin endpoints")
    void testAccessAdminEndpoints() throws Exception {
        // Giả sử có admin endpoints (nếu có)
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isNotFound()); // Endpoint không tồn tại hoặc 401
    }

    @Test
    @DisplayName("Auth Bypass Test 24: Create product without authentication")
    void testCreateProductWithoutAuth() throws Exception {
        Product product = Product.builder()
                .productName("Unauthorized Product")
                .price(999)
                .quantity(10)
                .description("Should not be created")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 25: Update other user's data")
    void testUpdateOtherUserData() throws Exception {
        // Thử update product của user khác
        Product product = Product.builder()
                .productName("Updated Product")
                .price(999)
                .quantity(10)
                .description("Unauthorized update")
                .category(Category.LAPTOPS)
                .build();

        mockMvc.perform(put("/api/products/other-user-product-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== COMPREHENSIVE AUTH CHECK ====================

    @Test
    @DisplayName("Auth Bypass Test 26: Comprehensive authentication check")
    void testComprehensiveAuthenticationCheck() throws Exception {
        // Kiểm tra tất cả protected endpoints yêu cầu authentication

        // GET
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        // POST
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // PUT
        mockMvc.perform(put("/api/products/some-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // DELETE
        mockMvc.perform(delete("/api/products/some-id"))
                .andExpect(status().isUnauthorized());

        // Auth endpoints should be accessible
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isBadRequest()); // Not 401 - endpoint is accessible
    }
}