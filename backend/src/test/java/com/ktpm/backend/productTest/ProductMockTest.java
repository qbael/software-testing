package com.ktpm.backend.productTest;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.exception.ProductNotFoundException;
import com.ktpm.backend.repository.ProductRepository;
import com.ktpm.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceMockTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo @Mock và @InjectMocks
        sampleId = UUID.randomUUID();
        sampleProduct = Product.builder()
                .id(sampleId)
                .productName("Laptop")
                .price(15000000)
                .quantity(10)
                .description("Gaming laptop")
                .category(Category.LAPTOPS)
                .build();
    }

    // ===================== GET PRODUCT =====================
    @Test
    @DisplayName("getProduct() - trả sản phẩm khi tồn tại")
    void testGetProduct() {
        when(productRepository.findById(sampleId)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProduct(sampleId).orElse(null);

        assertNotNull(result);
        assertEquals("Laptop", result.getProductName());

        verify(productRepository, times(1)).findById(sampleId);
    }

    @Test
    @DisplayName("getProduct() - ném ProductNotFoundException khi không tồn tại")
    void testGetProductNotFound() {
        when(productRepository.findById(sampleId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(sampleId));

        verify(productRepository, times(1)).findById(sampleId);
    }

    // ===================== CREATE PRODUCT =====================
    @Test
    @DisplayName("createProduct() - lưu sản phẩm mới")
    void testCreateProduct() {
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = productService.createProduct(sampleProduct);

        assertNotNull(result);
        assertEquals(sampleId, result.getId());

        verify(productRepository, times(1)).save(sampleProduct);
    }

    // ===================== UPDATE PRODUCT =====================
    @Test
    @DisplayName("updateProduct() - cập nhật sản phẩm thành công")
    void testUpdateProduct() {
        Product updatedData = Product.builder()
                .productName("Laptop Pro")
                .price(20000000)
                .quantity(5)
                .description("High-end laptop")
                .category(Category.LAPTOPS)
                .build();

        when(productRepository.findById(sampleId)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(sampleId, updatedData);

        assertEquals("Laptop Pro", result.getProductName());
        assertEquals(20000000, result.getPrice());
        assertEquals(5, result.getQuantity());

        verify(productRepository, times(1)).findById(sampleId);
        verify(productRepository, times(1)).save(sampleProduct);
    }

    // ===================== DELETE PRODUCT =====================
    @Test
    @DisplayName("deleteProduct() - xóa sản phẩm thành công")
    void testDeleteProduct() {
        when(productRepository.findById(sampleId)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        assertDoesNotThrow(() -> productService.deleteProduct(sampleId));

        verify(productRepository, times(1)).findById(sampleId);
        verify(productRepository, times(1)).delete(sampleProduct);
    }
}

