package com.ktpm.backend.service;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.exception.ProductNotFoundException;
import com.ktpm.backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Page<Product> getAll(Pageable pageable) {
        try {
            return productRepository.findAll(pageable);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách sản phẩm");
        }
    }

    public Optional<Product> getProduct(UUID id) {
        return Optional.ofNullable(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm")));
    }

    public Product createProduct(Product product) {
        try {
            return productRepository.save(product);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo sản phẩm");
        }
    }

    public Product updateProduct(UUID id, Product product) {
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm"));

        oldProduct.setProductName(product.getProductName());
        oldProduct.setPrice(product.getPrice());
        oldProduct.setQuantity(product.getQuantity());
        oldProduct.setDescription(product.getDescription());
        oldProduct.setCategory(product.getCategory());
        try {
            return productRepository.save(oldProduct);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi cập nhật sản phẩm");
        }

    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm"));
        try {
            productRepository.delete(product);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa sản phẩm");
        }
    }
}
