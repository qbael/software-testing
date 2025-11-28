package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - CSRF Protection")
public class CsrfSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String csrfToken;

    @BeforeEach
    void setUp() throws Exception {
        String username = "csrfuser123";
        String password = "CsrfPass123";

        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername(username);
        registerRequest.setPassword(password);
        registerRequest.setVerifyPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 201 || status == 409,
                            "Expected status 201 or 409 but got: " + status);
                });

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        token = Objects.requireNonNull(mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getCookie("token"))
                .getValue();

        MvcResult result = mockMvc.perform(get("/api/auth/current")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        if (csrfCookie != null) {
            csrfToken = csrfCookie.getValue();
        }
    }

    @Test
    @DisplayName("CSRF Test 1: POST request không có CSRF token sẽ bị từ chối")
    void testPostWithoutCsrfToken_ShouldFail() throws Exception {
        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .cookie(new Cookie("token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF Test 2: PUT request không có CSRF token sẽ bị từ chối")
    void testPutWithoutCsrfToken_ShouldFail() throws Exception {
        Product product = Product.builder()
                .productName("Updated Product")
                .price(200)
                .quantity(20)
                .description("Updated description")
                .category(Category.LAPTOPS)
                .build();

        mockMvc.perform(put("/api/products/a98e82bc-51ed-458e-8dd1-47ecb8c32bac")
                        .cookie(new Cookie("token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF Test 3: DELETE request không có CSRF token sẽ bị từ chối")
    void testDeleteWithoutCsrfToken_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/products/a98e82bc-51ed-458e-8dd1-47ecb8c32bac")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF Test 4: GET request sẽ không yêu cầu CSRF token")
    void testGetRequestWithoutCsrfToken_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }
}