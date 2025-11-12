package com.ktpm.backend.utils;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;

import java.util.Arrays;

public class Validator {

    public static boolean isBlank(String str) {
        str = str == null ? null : str.trim();
        return str == null || str.trim().isEmpty();
    }

    public static boolean isSizeInRange(String str, int min, int max) {
        if (str == null) return false;
        int length = str.length();
        return length >= min && length <= max;
    }

    public static boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z\\d._-]{3,50}$";
        username = username == null ? null : username.trim();
        return username != null && !username.isEmpty() && username.matches(usernameRegex);
    }

    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,100}$";
        password = password == null ? null : password.trim();
        return password != null && !password.isEmpty() && password.matches(passwordRegex);
    }

    public static boolean isValidProduct(Product product) {
        if (product == null) return false;

        boolean isProductNameValid = !isBlank(product.getProductName()) &&
                isSizeInRange(product.getProductName(), 3, 100);

        boolean isPriceValid = product.getPrice() != null &&
                product.getPrice().doubleValue() >= 0.01 &&
                product.getPrice().doubleValue() <= 999999999;

        boolean isQuantityValid = product.getQuantity() != null &&
                product.getQuantity() >= 0 &&
                product.getQuantity() <= 99999;

        boolean isDescriptionValid = product.getDescription() == null ||
                isSizeInRange(product.getDescription(), 0, 500);


        boolean isCategoryValid = product.getCategory() != null &&
                isValidCategory(product.getCategory().name());

        return isProductNameValid && isPriceValid && isQuantityValid && isDescriptionValid && isCategoryValid;
    }

    public static boolean isValidCategory(String value) {
        return Arrays.stream(Category.values())
                .anyMatch(c -> c.name().equalsIgnoreCase(value));
    }
}
