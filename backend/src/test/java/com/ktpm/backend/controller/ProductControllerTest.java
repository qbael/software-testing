package com.ktpm.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.exception.ProductNotFoundException;
import com.ktpm.backend.service.ProductService;
import org.junit.jupiter.api.*;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController API Tests")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;
    @Autowired private ObjectMapper objectMapper;

    private Product product;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        product = Product.builder()
                .id(id)
                .productName("MacBook Pro")
                .price(1999)
                .quantity(20)
                .description("High-end laptop".repeat(10)) // < 500 chars
                .category(Category.LAPTOPS)
                .build();
    }

    @Test
    @DisplayName("GET /api/products - Lấy danh sách có phân trang")
    void getAllProducts_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("limit", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("MacBook Pro"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getAll(any(Pageable.unpaged());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Tìm thấy sản phẩm")
    void getProductById_Success() throws Exception {
        when(productService.getProduct(id)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("MacBook Pro"));

        verify(productService).getProduct(id);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Không tìm thấy → 404")
    void getProductById_NotFound() throws Exception {
        when(productService.getProduct(id)).thenThrow(new ProductNotFoundException("Không tìm thấy"));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());

        verify(productService).getProduct(id);
    }

    @Test
    @DisplayName("POST /api/products - Tạo sản phẩm hợp lệ → 200")
    void createProduct_Valid_ReturnsCreatedProduct() throws Exception {
        when(productService.createProduct(any())).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("MacBook Pro"));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products - Tên sản phẩm rỗng → 400")
    void createProduct_EmptyName_ReturnsBadRequest() throws Exception {
        product.setProductName("");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products - Tên 2 ký tự → 400")
    void createProduct_NameTooShort_ReturnsBadRequest() throws Exception {
        product.setProductName("AB");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products - Giá <= 0 → 400")
    void createProduct_PriceZero_ReturnsBadRequest() throws Exception {
        product.setPrice(0);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products - Quantity âm → 400")
    void createProduct_NegativeQuantity_ReturnsBadRequest() throws Exception {
        product.setQuantity(-5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Cập nhật thành công")
    void updateProduct_Success() throws Exception {
        Product updated = Product.builder()
                .productName("MacBook Air M2")
                .price(1099)
                .quantity(100)
                .description("Lightweight")
                .category(Category.LAPTOPS)
                .build();

        when(productService.updateProduct(eq(id), any())).thenReturn(updated);

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("MacBook Air M2"));

        verify(productService).updateProduct(eq(id), any());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Không tìm thấy → 404")
    void deleteProduct_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new ProductNotFoundException("Không tìm thấy sản phẩm"))
                .when(productService).deleteProduct(id);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(id);
    }

    @Test
    @DisplayName("POST /api/products - Description > 500 ký tự → 400")
    void createProduct_DescriptionTooLong_ReturnsBadRequest() throws Exception {
        String longDesc = "a".repeat(501);
        product.setDescription(longDesc);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products - Category null → 400")
    void createProduct_NullCategory_ReturnsBadRequest() throws Exception {
        product.setCategory(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }
}