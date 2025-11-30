
package com.ktpm.backend.service;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;
import com.ktpm.backend.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Repository Mock & Behavior Tests")
class ProductServiceMockTest {

    @Mock   private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private UUID validId = UUID.randomUUID();

    @Test @DisplayName("getProductById - Tìm thấy")
    void getProductById_Found() {
        Product mockProduct = Product.builder()
                .id(validId)
                .productName("Laptop")
                .price(15000000)
                .category(Category.LAPTOPS)
                .build();

        when(productRepository.findById(validId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.getProduct(validId);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getProductName());

        verify(productRepository).findById(validId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test @DisplayName("createProduct → chỉ gọi save, không gọi findById")
    void createProduct_OnlyCallsSave() {
        Product newProduct = Product.builder().productName("Test Product").build();
        when(productRepository.save(any())).thenReturn(newProduct);

        productService.createProduct(newProduct);

        verify(productRepository, times(1)).save(newProduct);
        verify(productRepository, never()).findById(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test @DisplayName("updateProduct → gọi đúng thứ tự: findById → save")
    void updateProduct_CallsFindThenSave() {
        Product existing = Product.builder().id(validId).productName("Old").build();
        when(productRepository.findById(validId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenReturn(existing);

        productService.updateProduct(validId, existing);

        InOrder inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findById(validId);
        inOrder.verify(productRepository).save(existing);
        verifyNoMoreInteractions(productRepository);
    }

    @Test @DisplayName("deleteProduct → gọi findById rồi delete")
    void deleteProduct_CallsFindAndDelete() {
        when(productRepository.findById(validId)).thenReturn(Optional.of(Product.builder().id(validId).build()));

        productService.deleteProduct(validId);

        verify(productRepository).findById(validId);
        verify(productRepository).delete(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test @DisplayName("getAll → chỉ gọi findAll")
    void getAll_OnlyCallsFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findAll(pageable)).thenReturn(Page.empty());

        productService.getAll(pageable);

        verify(productRepository).findAll(pageable);
        verifyNoMoreInteractions(productRepository);
    }
}