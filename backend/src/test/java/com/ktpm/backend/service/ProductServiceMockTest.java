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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService - Repository Mock & Behavior Tests")
class ProductServiceMockTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private UUID validId = UUID.randomUUID();


    @Test
    @DisplayName("getProductById - Tìm thấy")
    void getProductById_Found() {
        Product mockProduct = Product.builder()
                .id(validId)
                .productName("Laptop")
                .price(15000000)
                .category(Category.LAPTOPS)
                .build();

        when(productRepository.findById(validId)).thenReturn(Optional.of(mockProduct));

        Optional<Product> result = productService.getProduct(validId);

        // Assert business logic
        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getProductName());
        assertEquals(15000000, result.get().getPrice());

        // Verify interactions
        verify(productRepository).findById(validId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("createProduct → chỉ gọi save, không gọi findById")
    void createProduct_OnlyCallsSave() {
        Product newProduct = Product.builder().productName("Test Product").build();
        when(productRepository.save(any())).thenReturn(newProduct);

        Product result = productService.createProduct(newProduct);

        // Assert business logic
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());

        // Verify interactions
        verify(productRepository, times(1)).save(newProduct);
        verify(productRepository, never()).findById(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("updateProduct → gọi đúng thứ tự: findById → save")
    void updateProduct_CallsFindThenSave() {
        Product existing = Product.builder().id(validId).productName("Old").build();
        Product updated = Product.builder().id(validId).productName("New").build();

        when(productRepository.findById(validId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenReturn(updated);

        Product result = productService.updateProduct(validId, updated);

        // Assert business logic
        assertNotNull(result);
        assertEquals("New", result.getProductName());

        // Verify order
        InOrder inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findById(validId);
        inOrder.verify(productRepository).save(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("deleteProduct → gọi findById rồi delete")
    void deleteProduct_CallsFindAndDelete() {
        when(productRepository.findById(validId))
                .thenReturn(Optional.of(Product.builder().id(validId).build()));

        productService.deleteProduct(validId);

        verify(productRepository).findById(validId);
        verify(productRepository).delete(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("getAll → chỉ gọi findAll")
    void getAll_OnlyCallsFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(
                Product.builder().productName("Product 1").build()
        );
        Page<Product> page = new PageImpl<>(products);

        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.getAll(pageable);

        // Assert business logic
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Product 1", result.getContent().get(0).getProductName());

        // Verify interactions
        verify(productRepository).findAll(pageable);
        verifyNoMoreInteractions(productRepository);
    }


    @Test
    @DisplayName("VERIFY: Không gọi methods không cần thiết")
    void verifyNoUnnecessaryCalls() {
        when(productRepository.findById(validId))
                .thenReturn(Optional.of(Product.builder().build()));

        productService.getProduct(validId);

        verify(productRepository).findById(validId);
        verify(productRepository, never()).save(any());
        verify(productRepository, never()).delete(any());
        verifyNoMoreInteractions(productRepository);
    }
}