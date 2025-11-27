package com.ktpm.backend.utils;

import com.ktpm.backend.entity.Product;
import com.ktpm.backend.entity.enums.Category;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<script|javascript:|onload=|onerror=|onclick=|onmouseover=|onfocus=|onblur=|<iframe|</script>|eval\\(|alert\\(|document\\.cookie|window\\.location|document\\.write",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "('(''|[^'])*')|(;)|(--[\\r\\n]|--)|(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|CREATE|ALTER|EXEC|EXECUTE|MERGE|TRUNCATE|GRANT|REVOKE)\\b)",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isBlank(String str) {
        str = str == null ? null : str.trim();
        return str == null || str.isBlank();
    }


    public static boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9._-]{3,50}$";
        boolean isNullOrBlank = isBlank(username);

        if (isNullOrBlank) {
            return false;
        }

        if (containsXSS(username) || containsSqlInjection(username)) {
            return false;
        }

        return username.matches(usernameRegex);
    }

    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,100}$";
        boolean isNullOrBlank = isBlank(password);

        if (isNullOrBlank) {
            return false;
        }

        if (containsXSS(password) || containsSqlInjection(password)) {
            return false;
        }

        return password.matches(passwordRegex);
    }

    public static boolean isSizeInRange(String str, int min, int max) {
        if (str == null) return false;
        int length = str.length();
        return length >= min && length <= max;
    }

    public static boolean isValidProduct(Product product) {
        if (product == null) return false;

        if (containsXSS(product.getProductName()) || containsSqlInjection(product.getProductName()) ||
                containsXSS(product.getDescription()) || containsSqlInjection(product.getDescription())) {
            return false;
        }

        boolean isProductNameValid = !isBlank(product.getProductName()) &&
                isSizeInRange(product.getProductName(), 3, 100);

        boolean isPriceValid = product.getPrice() != null &&
                product.getPrice().doubleValue() >= 0.01 &&
                product.getPrice().doubleValue() <= 999999999;

        boolean isQuantityValid = product.getQuantity() != null &&
                product.getQuantity() >= 0 &&
                product.getQuantity() <= 99999;

        boolean isDescriptionValid = !isBlank(product.getDescription()) &&
                isSizeInRange(product.getDescription(), 0, 500);

        boolean isCategoryValid = product.getCategory() != null &&
                isValidCategory(product.getCategory().name());

        return isProductNameValid && isPriceValid && isQuantityValid && isDescriptionValid && isCategoryValid;
    }

    public static boolean isValidCategory(String value) {
        return Arrays.stream(Category.values())
                .anyMatch(c -> c.name().equalsIgnoreCase(value));
    }

    public static boolean containsXSS(String input) {
        return XSS_PATTERN.matcher(input).find();
    }

    public static boolean containsSqlInjection(String input) {
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static String sanitizeInput(String input) {

        String sanitized = input
                .replaceAll("<script.*?</script>", "")
                .replaceAll("<.*?>", "")
                .replaceAll("javascript:", "")
                .replaceAll("onload=", "")
                .replaceAll("onerror=", "")
                .replaceAll("onclick=", "")
                .replaceAll("eval\\(", "");

        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");

        sanitized = sanitized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");

        return sanitized.trim();
    }

    public static Product sanitizeProduct(Product product) {
        if (product == null) {
            return null;
        }

        Product sanitizedProduct = new Product();
        sanitizedProduct.setId(product.getId());
        sanitizedProduct.setProductName(sanitizeInput(product.getProductName()));
        sanitizedProduct.setPrice(product.getPrice());
        sanitizedProduct.setQuantity(product.getQuantity());
        sanitizedProduct.setDescription(sanitizeInput(product.getDescription()));
        sanitizedProduct.setCategory(product.getCategory());

        return sanitizedProduct;
    }

    public static String sanitizeApiParameter(String param) {
        if (isBlank(param)) {
            return param;
        }

        return param.replaceAll("[^a-zA-Z0-9-_]", "");
    }
}