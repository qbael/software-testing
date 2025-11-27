export function validateProduct(product) {
    const errors = {};

    if (!product.productName || product.productName.trim().length < 3 || product.productName.length > 100) {
        errors.productName = 'Tên sản phẩm phải từ 3-100 ký tự';
    }

    if (!product.price || product.price <= 0) {
        errors.price = 'Giá phải lớn hơn 0';
    }

    if (product.quantity < 0) {
        errors.quantity = 'Số lượng phải >= 0';
    }

    if (product.description && product.description.length > 500) {
        errors.description = 'Mô tả không được vượt quá 500 ký tự';
    }

    const validCategories = ['SMARTPHONE', 'LAPTOPS', 'HEADPHONES', 'CAMERAS'];
    if (!validCategories.includes(product.category)) {
        errors.category = 'Danh mục không hợp lệ';
    }

    return errors;
}