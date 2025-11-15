import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import ProductForm from "../../../src/components/Forms/Forms.jsx";
import { addProductModel } from "../../../src/models/addProductFormModel.js";
import { updateProductModel } from "../../../src/models/updateProductFormModel.js";
import * as api from "../../../src/api/productAPI.js";

jest.mock("../../../src/api/productAPI.js");

describe("ProductForm Integration Tests", () => {
    const productData = {
        productName: "Laptop Dell",
        price: 15000000,
        quantity: 10,
        description: "Laptop mỏng nhẹ",
        category: "LAPTOPS"
    };

    beforeEach(() => {
        jest.clearAllMocks();
        // Mock alert để test không fail
        window.alert = jest.fn();
    });

    test("Tạo sản phẩm mới thành công", async () => {
        // Mock createProduct API trả về thành công
        api.createProduct.mockResolvedValueOnce({ data: { id: 1, ...productData } });
        const mockOnSubmit = jest.fn(async (payload) => {
            await api.createProduct(payload);
            window.alert("Thêm sản phẩm thành công");
        });
        render(<ProductForm formModel={addProductModel} onSubmit={mockOnSubmit} />);

        // Điền form
        fireEvent.change(screen.getByLabelText(/Product Name/i), {
            target: { value: productData.productName }
        });
        fireEvent.change(screen.getByLabelText(/Price/i), { target: { value: productData.price.toString() } });
        fireEvent.change(screen.getByLabelText(/Quantity/i), { target: { value: productData.quantity.toString() } });
        fireEvent.change(screen.getByLabelText(/Description/i), { target: { value: productData.description } });
        fireEvent.change(screen.getByLabelText(/Category/i), { target: { value: productData.category } });
        // Submit form
        fireEvent.click(screen.getByRole('button', { name: addProductModel.formName }));

        // Chờ API gọi và alert được gọi
        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalled();
            expect(window.alert).toHaveBeenCalledWith("Thêm sản phẩm thành công");
        });
    });

    test("Chỉnh sửa sản phẩm thành công", async () => {
        const existingProduct = { id: 1, ...productData };
        const updatedData = { ...productData, price: 14000000 };

        api.updateProduct.mockResolvedValueOnce({ data: updatedData });

        const mockOnSubmit = jest.fn(async (payload) => {
            await api.updateProduct(existingProduct.id, payload);
            window.alert("update sản phẩm thành công");
        });

        render(
            <ProductForm
                object={existingProduct}
                formModel={updateProductModel}
                onSubmit={mockOnSubmit}
            />
        );

        // Thay đổi giá
        fireEvent.change(screen.getByLabelText(/Price/i), {
            target: { value: updatedData.price.toString() }
        });

        // Submit form (button của update)
        fireEvent.click(screen.getByRole('button', { name: updateProductModel.formName }));

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalled();
            expect(api.updateProduct).toHaveBeenCalledWith(existingProduct.id, updatedData);
            expect(window.alert).toHaveBeenCalledWith("update sản phẩm thành công");
        });
    });


});

