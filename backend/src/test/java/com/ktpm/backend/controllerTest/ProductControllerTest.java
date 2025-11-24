package com.ktpm.backend.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.controller.ProductController;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.exception.ProductNotFoundException;
import com.ktpm.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID productId;
    private Product sampleProduct;
    private Product updateData;
    private Pageable pageable;
    private Page<Product> samplePage;

    @BeforeEach
    void init() {
        productId = UUID.randomUUID();

        sampleProduct = Product.builder()
                .id(productId)
                .productName("Old Name")
                .price(50)
                .quantity(5)
                .description("Old Desc")
                .category(Category.SMARTPHONE) // enum hợp lệ
                .build();

        updateData = Product.builder()
                .productName("New Name")
                .price(100)
                .quantity(10)
                .description("New Desc")
                .category(Category.LAPTOPS) // enum hợp lệ
                .build();

        pageable = PageRequest.of(0, 10);
        samplePage = new PageImpl<>(List.of(sampleProduct));
    }

    /* ---------------- GET /api/products ---------------- */
    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAll(any(Pageable.class))).thenReturn(samplePage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(productId.toString()))
                .andExpect(jsonPath("$.content[0].productName").value("Old Name"));
    }

    /* ---------------- GET /api/products/{id} ---------------- */
    @Test
    void testGetProduct_Success() throws Exception {
        when(productService.getProduct(productId)).thenReturn(Optional.of(sampleProduct));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("Old Name"));
    }

    @Test
    void testGetProduct_NotFound() throws Exception {
        when(productService.getProduct(productId)).thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    /* ---------------- POST /api/products ---------------- */
    @Test
    void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.productName").value("Old Name"));
    }

    /* ---------------- PUT /api/products/{id} ---------------- */
    @Test
    void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(any(UUID.class), any(Product.class))).thenReturn(updateData);

        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("New Name"))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    void testUpdateProduct_NotFound() throws Exception {
        when(productService.updateProduct(any(UUID.class), any(Product.class)))
                .thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    /* ---------------- DELETE /api/products/{id} ---------------- */
    @Test
    void testDeleteProduct_Success() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteProduct_NotFound() throws Exception {
        Mockito.doThrow(new ProductNotFoundException("Not found"))
                .when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }
}
