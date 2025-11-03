package com.ktpm.backend.entity;

import com.ktpm.backend.entity.enums.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @NotBlank(message = "Tên sản phẩm không được để trống")
//    @Size(min = 3, max = 100, message = "Tên sản phẩm phải từ 3 đến 100 ký tự")
    @Column(name = "product_name")
    private String productName;

//    @NotNull(message = "Giá thành không được để trống")
//    @DecimalMin(value = "0.01", message = "Giá thành phải lớn hơn 0")
//    @DecimalMax(value = "999999999", message = "Tên sản phẩm phải nhỏ hơn hoặc bằng 999,999,999")
    @Column(name = "price")
    private BigDecimal price;

//    @NotNull(message = "Số lượng không được để trống")
//    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
//    @Max(value = 99999, message = "Số lượng phải nhỏ hơn hoặc bằng 99,999")
    @Column(name = "quantity")
    private Integer quantity;

//    @Size(max = 500, message = "Description không được vượt quá 500 ký tự")
    @Column(name = "description")
    private String description;

//    @NotNull(message = "Category không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;
}
