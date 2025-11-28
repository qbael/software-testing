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

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - XSS Prevention")
public class XssSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String username = "testuser123";
        String password = "testuser123";

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
    }

    @Test
    @DisplayName("XSS Test 1: Login với username chứa mã độc XSS")
    void testXssInLoginUsername() throws Exception {
        String malicious = "<script>alert('xss123')</script>";

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername(malicious);
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(malicious))));
    }

    @Test
    @DisplayName("XSS Test 2: Request header và cookie chứa mã độc XSS")
    void testXssInRequestHeader() throws Exception {
        String malicious = "<script>alert('xss123')</script>";

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser123");
        loginRequest.setPassword("testuser123");

        String token = Objects.requireNonNull(mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getCookie("token"))
                .getValue();


        Product product = Product.builder()
                .productName("Test Product")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product))
                        .header("Authorization", "Bearer " + token + malicious)
                        .cookie(new Cookie("token", token + malicious)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Authorization", not(containsString(malicious))))
                .andExpect(content().string(not(containsString(malicious))));
    }

    @Test
    @DisplayName("XSS Test 3: Tạo sản phẩm với tên chứa mã độc XSS")
    void testXssInCreateProductName() throws Exception {
        String malicious = "<script>alert('xss123')</script>";

        Product product = Product.builder()
                .productName(malicious)
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product))
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(malicious))));
    }
}
