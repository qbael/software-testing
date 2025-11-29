package com.ktpm.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.repository.ProductRepository;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;

    private static UUID createdId;

    @Test
    @Order(1)
    @DisplayName("INTEGRATION - Tạo sản phẩm thật vào H2")
    void createProduct_RealDatabase() throws Exception {
        Product newProduct = Product.builder()
                .productName("Test Integration Product")
                .price(999)
                .quantity(99)
                .description("This product created in integration test")
                .category(Category.SMARTPHONE)
                .build();

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Lấy ID từ response
        createdId = UUID.fromString(
                objectMapper.readTree(response).get("id").asText()
        );

        assertNotNull(createdId);
    }

    @Test
    @Order(2)
    @DisplayName("INTEGRATION - Lấy sản phẩm vừa tạo")
    void getProductById_AfterCreate() throws Exception {
        mockMvc.perform(get("/api/products/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Test Integration Product"));
    }

    @Test
    @Order(3)
    @DisplayName("INTEGRATION - Lấy tất cả sản phẩm")
    void getAllProducts_ContainsCreatedProduct() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @Order(4)
    @DisplayName("INTEGRATION - Cập nhật sản phẩm")
    void updateProduct_AfterCreate() throws Exception {
        Product updateProduct = Product.builder()
                .id(createdId)
                .productName("Updated Integration Product")
                .price(1999)
                .quantity(199)
                .description("This product has been updated")
                .category(Category.LAPTOPS)
                .build();

        mockMvc.perform(put("/api/products/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Integration Product"))
                .andExpect(jsonPath("$.price").value(1999))
                .andExpect(jsonPath("$.quantity").value(199))
                .andExpect(jsonPath("$.description").value("This product has been updated"))
                .andExpect(jsonPath("$.category").value("LAPTOPS"));
    }

    @Test
    @Order(5)
    @DisplayName("INTEGRATION - Xác nhận sản phẩm đã được cập nhật")
    void getProductById_AfterUpdate() throws Exception {
        mockMvc.perform(get("/api/products/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Integration Product"))
                .andExpect(jsonPath("$.price").value(1999));
    }

    @Test
    @Order(6)
    @DisplayName("INTEGRATION - Xóa sản phẩm")
    void deleteProduct_AfterCreate() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", createdId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/{id}", createdId))
                .andExpect(status().isNotFound());
    }
}