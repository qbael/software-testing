package com.ktpm.backend.service;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.exception.ProductNotFoundException;
import com.ktpm.backend.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private UUID validId;
    private UUID invalidId;

    @BeforeEach
    void setUp() {
        validId = UUID.randomUUID();
        invalidId = UUID.randomUUID();

        product = Product.builder()
                .id(validId)
                .productName("iPhone 15 Pro")
                .price(1299)
                .quantity(50)
                .description("Flagship phone 2024")
                .category(Category.SMARTPHONE)
                .build();

    }

    @Test
    @DisplayName("getAll() - Trả về danh sách có phân trang thành công")
    void getAll_PagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15 Pro", result.getContent().get(0).getProductName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getProduct() - Tìm thấy sản phẩm theo ID")
    void getProduct_ExistingId_ReturnsProduct() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProduct(validId);

        assertTrue(result.isPresent());
        assertEquals("iPhone 15 Pro", result.get().getProductName());
        verify(productRepository).findById(validId);
    }

    @Test
    @DisplayName("getProduct() - Không tìm thấy → ném ProductNotFoundException")
    void getProduct_NotFound_ThrowsException() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProduct(invalidId)
        );

        assertEquals("Không tìm thấy sản phẩm", exception.getMessage());
        verify(productRepository).findById(invalidId);
    }

    @Test
    @DisplayName("createProduct() - Tạo sản phẩm thành công")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(product);

        assertNotNull(result.getId());
        assertEquals("iPhone 15 Pro", result.getProductName());
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("creatProduct() - Lỗi DB -> ném Exception")
    void createProduct_Exception() {
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("DB connection failed"));

        assertThrows(RuntimeException.class, () -> productService.createProduct(product));
        verify(productRepository).save(any());
    }

    @Test
    @DisplayName("updateProduct() - Cập nhập thành công")
    void updateProduct_Success() {
        Product updatedInfo = Product.builder()
                .productName("iPhone 16 Pro")
                .price(1499)
                .quantity(30)
                .description("New model")
                .category(Category.SMARTPHONE)
                .build();

        when(productRepository.findById(validId)).thenReturn(Optional.of(updatedInfo));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(validId, updatedInfo);
        assertEquals("iPhone 16 Pro", result.getProductName());
        assertEquals(1499, result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct() - Không tìm thấy → ném exception")
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.updateProduct(invalidId, product));
    }

    @Test
    @DisplayName("deleteProduct() - Xóa thành công")
    void deleteProduct_Success() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(product));

        productService.deleteProduct(validId);

        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteProduct() - Không tìm thấy → ném exception")
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.deleteProduct(invalidId));
        verify(productRepository, never()).delete(any());
    }

}
