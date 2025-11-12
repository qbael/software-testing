// src/api/productApi.js
import axiosClient from "./axiosClient";

// Lấy danh sách sản phẩm (có thể truyền page & size để phân trang)
export async function getProducts(page = 0, limit = 10,sortBy= 'id',sortDir = 'asc') {
    return axiosClient.get(`/products`, {
        params: { page, limit, sortBy, sortDir },
    });
}

// Lấy chi tiết 1 sản phẩm theo ID
export async function getProductById(id) {
    const res = await axiosClient.get(`/products/${id}`);
    return res.data;
}

// Tạo sản phẩm mới
export async function createProduct(product) {
    if (product.price !== undefined) product.price = Number(product.price);
    if (product.quantity !== undefined) product.quantity = Number(product.quantity);

    const res = await axiosClient.post("/products", product);
    return res.data;
}


// Cập nhật sản phẩm
export async function updateProduct(id, product) {
    const res = await axiosClient.put(`/products/${id}`, product);
    return res.data;
}

// Xóa sản phẩm
export async function deleteProduct(id) {
    const res = await axiosClient.delete(`/products/${id}`);
    return res.data;
}
