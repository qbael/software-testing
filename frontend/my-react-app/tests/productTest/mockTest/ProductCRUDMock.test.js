import * as productService from "../../../src/api/productAPI.js";
jest.mock("../../../src/api/productAPI.js");

describe('ProductService Mock Tests', () => {
    const mockProduct = {
        id: 1,
        name: 'Laptop Dell',
        price: 15000000,
        quantity: 10,
    };

    const mockProducts = [
        { id: 1, name: 'Laptop Dell', price: 15000000, quantity: 10 },
        { id: 2, name: 'Headphone Sony', price: 2000000, quantity: 5 },
    ];

    beforeEach(() => {
        jest.clearAllMocks();
    });

    // CREATE
    test('Mock: Create product thành công', async () => {
        productService.createProduct.mockResolvedValue(mockProduct);

        const result = await productService.createProduct(mockProduct);

        expect(result).toEqual(mockProduct);
        expect(productService.createProduct).toHaveBeenCalledWith(mockProduct);
        expect(productService.createProduct).toHaveBeenCalledTimes(1);
    });

    test('Mock: Create product thất bại', async () => {
        const error = new Error('Create failed');
        productService.createProduct.mockRejectedValue(error);

        await expect(productService.createProduct(mockProduct)).rejects.toThrow('Create failed');
        expect(productService.createProduct).toHaveBeenCalledTimes(1);
    });

    // READ
    test('Mock: Get products thành công', async () => {
        productService.getProducts.mockResolvedValue(mockProducts);

        const result = await productService.getProducts({ page: 1, size: 10 });

        expect(result).toEqual(mockProducts);
        expect(productService.getProducts).toHaveBeenCalledWith({ page: 1, size: 10 });
        expect(productService.getProducts).toHaveBeenCalledTimes(1);
    });

    test('Mock: Get products thất bại', async () => {
        const error = new Error('Get failed');
        productService.getProducts.mockRejectedValue(error);

        await expect(productService.getProducts({ page: 1, size: 10 })).rejects.toThrow('Get failed');
        expect(productService.getProducts).toHaveBeenCalledTimes(1);
    });

    // UPDATE
    test('Mock: Update product thành công', async () => {
        const updatedProduct = { ...mockProduct, price: 14000000 };
        productService.updateProduct.mockResolvedValue(updatedProduct);

        const result = await productService.updateProduct(mockProduct.id, updatedProduct);

        expect(result).toEqual(updatedProduct);
        expect(productService.updateProduct).toHaveBeenCalledWith(mockProduct.id, updatedProduct);
        expect(productService.updateProduct).toHaveBeenCalledTimes(1);
    });

    test('Mock: Update product thất bại', async () => {
        const error = new Error('Update failed');
        productService.updateProduct.mockRejectedValue(error);

        await expect(productService.updateProduct(mockProduct.id, mockProduct)).rejects.toThrow('Update failed');
        expect(productService.updateProduct).toHaveBeenCalledTimes(1);
    });

    // DELETE
    test('Mock: Delete product thành công', async () => {
        productService.deleteProduct.mockResolvedValue({ success: true });

        const result = await productService.deleteProduct(mockProduct.id);

        expect(result).toEqual({ success: true });
        expect(productService.deleteProduct).toHaveBeenCalledWith(mockProduct.id);
        expect(productService.deleteProduct).toHaveBeenCalledTimes(1);
    });

    test('Mock: Delete product thất bại', async () => {
        const error = new Error('Delete failed');
        productService.deleteProduct.mockRejectedValue(error);

        await expect(productService.deleteProduct(mockProduct.id)).rejects.toThrow('Delete failed');
        expect(productService.deleteProduct).toHaveBeenCalledTimes(1);
    });
});
