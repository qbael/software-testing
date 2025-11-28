package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - Authentication Bypass Prevention")
public class AuthenticationBypassSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Auth Bypass Test 1: Truy cập API mà không có token")
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or missing token"));
    }

    @Test
    @DisplayName("Auth Bypass Test 2: JWT giả mạo")
    void testTamperedJwtSignature() throws Exception {
        String tamperedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.TAMPERED_SIGNATURE";

        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("jwt", tamperedToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 3: Mã hóa URL để truy cập API")
    void testUrlEncodingBypass() throws Exception {
        mockMvc.perform(get("/api/%70roducts"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Auth Bypass Test 4: Host header injection")
    void testHostHeaderInjection() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Host", "evil.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Auth Bypass Test 5: Cookie injection")
    void testCookieInjection() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("token", "fake-token"))
                        .cookie(new Cookie("admin", "true"))
                        .cookie(new Cookie("authenticated", "true")))
                .andExpect(status().isUnauthorized());
    }
}