// src/api/productApi.js
import axiosClient from "./axiosClient";

// Lấy danh sách sản phẩm (có thể truyền page & size để phân trang)
export async function getProducts(page = 0, size = 10) {
    const res = await axiosClient.get(`/products?page=${page}&size=${size}`);
    return res.data; // backend trả về Page<Product>
}

// Lấy chi tiết 1 sản phẩm theo ID
export async function getProductById(id) {
    const res = await axiosClient.get(`/products/${id}`);
    return res.data;
}

// Tạo sản phẩm mới
export async function createProduct(product) {
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
