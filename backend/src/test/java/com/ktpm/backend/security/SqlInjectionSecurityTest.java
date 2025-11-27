package com.ktpm.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.config.SecurityConfig;
import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("Security Test - SQL Injection Prevention")
public class SqlInjectionSecurityTest {

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
    @DisplayName("SQL Injection Test 1: Login với username chứa SQL command")
    void testSqlInjectionInLoginUsername() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser123' OR '1'='1");
        loginRequest.setPassword("testuser123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("SQL"))))
                .andExpect(content().string(not(containsString("syntax"))));
    }

    @Test
    @DisplayName("SQL Injection Test 2: Login với password chứa SQL command")
    void testSqlInjectionInLoginPassword() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("' OR '1'='1' --");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("SQL"))));
    }

    @Test
    @DisplayName("SQL Injection Test 3: Create product với SQL command trong tên")
    void testSqlInjectionInProductName() throws Exception {
        Product product = Product.builder()
                .productName("'; DROP TABLE products; --")
                .price(100)
                .quantity(10)
                .description("Test description")
                .category(Category.SMARTPHONE)
                .build();

        mockMvc.perform(post("/api/products")
                        .cookie(new Cookie("token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SQL Injection Test 4: Search products với SQL injection trong sortBy")
    void testSqlInjectionInSortByParameter() throws Exception {
        mockMvc.perform(get("/api/products")
                        .cookie(new Cookie("token", token))
                        .param("page", "0")
                        .param("limit", "10")
                        .param("sortBy", "id; DROP TABLE products;--")
                        .param("sortDir", "asc"))
                .andExpect(status().isBadRequest());
    }
}