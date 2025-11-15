package com.ktpm.backend.productTest;

import com.ktpm.backend.controller.ProductController;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)     // BYPASS SECURITY
@DisplayName("Product API Integration Tests (Security Bypassed)")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    // ===================== READ ALL ======================
    @Test
    @WithMockUser   // BYPASS SECURITY
    @DisplayName("GET /api/products - Lấy danh sách sản phẩm")
    void testGetAllProducts() throws Exception {
        Product p1 = Product.builder()
                .id(UUID.randomUUID())
                .productName("Laptop")
                .price(15000000)
                .quantity(10)
                .description("Gaming laptop")
                .category(Category.LAPTOPS)
                .build();

        Product p2 = Product.builder()
                .id(UUID.randomUUID())
                .productName("Headphones")
                .price(500000)
                .quantity(50)
                .description("Wireless headphones")
                .category(Category.HEADPHONES)
                .build();

        Page<Product> pageProducts = new PageImpl<>(Arrays.asList(p1, p2));

        when(productService.getAll(any(Pageable.class))).thenReturn(pageProducts);

        ResultActions response = mockMvc.perform(get("/api/products"));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.content[1].productName").value("Headphones"));
    }

    // ===================== CREATE ======================
    @Test
    @WithMockUser   // BYPASS SECURITY
    @DisplayName("POST /api/products - Tạo sản phẩm mới")
    void testCreateProduct() throws Exception {
        UUID id = UUID.randomUUID();

        Product saved = Product.builder()
                .id(id)
                .productName("Smartphone X")
                .price(12000000)
                .quantity(15)
                .description("Flagship smartphone")
                .category(Category.SMARTPHONE)
                .build();

        when(productService.createProduct(any(Product.class))).thenReturn(saved);

        String jsonBody = """
                {
                  "productName": "Smartphone X",
                  "price": 12000000,
                  "quantity": 15,
                  "description": "Flagship smartphone",
                  "category": "SMARTPHONE"
                }
                """;

        ResultActions response = mockMvc.perform(
                post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        // Controller của bạn trả 200, nên test KHỚP status 200
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.productName").value("Smartphone X"));
    }

    // ===================== READ ONE ======================
    @Test
    @WithMockUser   // BYPASS SECURITY
    @DisplayName("GET /api/products/{id} - Lấy sản phẩm theo ID")
    void testGetProductById() throws Exception {
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
                .id(id)
                .productName("Laptop")
                .price(15000000)
                .quantity(10)
                .description("Gaming laptop")
                .category(Category.LAPTOPS)
                .build();

        when(productService.getProduct(eq(id))).thenReturn(Optional.of(product));

        ResultActions response = mockMvc.perform(get("/api/products/" + id));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    // ===================== UPDATE ======================
    @Test
    @WithMockUser   // BYPASS SECURITY
    @DisplayName("PUT /api/products/{id} - Cập nhật sản phẩm")
    void testUpdateProduct() throws Exception {
        UUID id = UUID.randomUUID();

        Product updated = Product.builder()
                .id(id)
                .productName("Laptop Pro")
                .price(20000000)
                .quantity(5)
                .description("High-end gaming laptop")
                .category(Category.LAPTOPS)
                .build();

        when(productService.updateProduct(eq(id), any(Product.class))).thenReturn(updated);

        String jsonBody = """
                {
                  "productName": "Laptop Pro",
                  "price": 20000000,
                  "quantity": 5,
                  "description": "High-end gaming laptop",
                  "category": "LAPTOPS"
                }
                """;

        ResultActions response = mockMvc.perform(
                put("/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        );

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Laptop Pro"))
                .andExpect(jsonPath("$.price").value(20000000));
    }

    // ===================== DELETE ======================
    @Test
    @WithMockUser   // BYPASS SECURITY
    @DisplayName("DELETE /api/products/{id} - Xóa sản phẩm")
    void testDeleteProduct() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(id);

        ResultActions response = mockMvc.perform(delete("/api/products/" + id));

        // Controller trả 200 → test theo 200
        response.andExpect(status().isOk());
    }
}



