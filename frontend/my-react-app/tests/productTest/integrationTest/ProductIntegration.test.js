import React, { useState } from "react";
import { render, screen, fireEvent, waitFor, within } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import ProductList from "../../../src/pages/productPage/productList.jsx";
import InfoBoard from "../../../src/components/InfoBoard/InfoBoard.jsx";
import ProductForm from "../../../src/components/Forms/Forms.jsx";
import ProductManagementPage from "../../../src/pages/productPage/product.jsx";
import { addProductModel } from "../../../src/models/addProductFormModel.js";
import { updateProductModel } from "../../../src/models/updateProductFormModel.js";
import * as api from "../../../src/api/productAPI.js";
import * as authApi from "../../../src/api/authApi.js";

jest.mock("../../../src/api/productAPI.js");
jest.mock("../../../src/api/authApi.js");

describe("Product Integration Tests", () => {
    const productData = {
        productName: "Laptop Dell",
        price: 15000000,
        quantity: 10,
        description: "Laptop mỏng nhẹ",
        category: "LAPTOPS"
    };

    const products = [
        { id: 1, productName: "Laptop Dell", price: 15000000, quantity: 10, description: "Mỏng nhẹ", category: "LAPTOPS" },
        { id: 2, productName: "Mouse Logitech", price: 200000, quantity: 50, description: "Chuột gaming", category: "HEADPHONES" }
    ];

    beforeEach(() => {
        jest.clearAllMocks();
        window.alert = jest.fn();
        authApi.getCurrentUser.mockResolvedValue({ username: "admin" });
        authApi.logout.mockResolvedValue();
    });

    describe("ProductDetail Integration Tests", () => {
        function Wrapper() {
            const [currProduct, setCurrProduct] = useState(null);
            const [displayBoard, setDisplayBoard] = useState(false);

            const handleCheck = (p) => {
                setCurrProduct(p);
                setDisplayBoard(true);
            };

            const handleClose = () => setDisplayBoard(false);

            return (
                <div>
                    <ProductList
                        products={products}
                        onDetailCheck={handleCheck}
                        onEdit={() => {}}
                        onDelete={() => {}}
                    />
                    {displayBoard && <InfoBoard product={currProduct} onClose={handleClose} />}
                </div>
            );
        }

        test("Hiển thị chi tiết sản phẩm khi click", () => {
            render(<Wrapper />);
            const icon = screen.getAllByAltText("icon")[0];
            fireEvent.click(icon);

            const board = screen.getByTestId("productDetail");
            expect(board).toBeInTheDocument();
            expect(within(board).getByText("Laptop Dell")).toBeInTheDocument();
            expect(within(board).getByText("15.000.000 ₫")).toBeInTheDocument();
            expect(within(board).getByText("LAPTOPS")).toBeInTheDocument();
            expect(within(board).getByText("10")).toBeInTheDocument();
            expect(within(board).getByText("Mỏng nhẹ")).toBeInTheDocument();
        });

        test("Đóng InfoBoard khi click ngoài", () => {
            render(<Wrapper />);

            const icon = screen.getAllByAltText("icon")[0];
            fireEvent.click(icon);

            const board = screen.getByTestId("productDetail");
            expect(board).toBeInTheDocument();

            fireEvent.mouseDown(document.body);

            expect(screen.queryByTestId("productDetail")).not.toBeInTheDocument();
        });
    });

    describe("ProductForm Integration Tests", () => {
        test("Tạo sản phẩm mới thành công", async () => {
            api.createProduct.mockResolvedValueOnce({ data: { id: 1, ...productData } });
            const mockOnSubmit = jest.fn(async (payload) => {
                await api.createProduct(payload);
                window.alert("Thêm sản phẩm thành công");
            });

            render(<ProductForm formModel={addProductModel} onSubmit={mockOnSubmit} />);

            fireEvent.change(screen.getByLabelText(/Product Name/i), {
                target: { value: productData.productName }
            });
            fireEvent.change(screen.getByLabelText(/Price/i), {
                target: { value: productData.price.toString() }
            });
            fireEvent.change(screen.getByLabelText(/Quantity/i), {
                target: { value: productData.quantity.toString() }
            });
            fireEvent.change(screen.getByLabelText(/Description/i), {
                target: { value: productData.description }
            });
            fireEvent.change(screen.getByLabelText(/Category/i), {
                target: { value: productData.category }
            });

            fireEvent.click(screen.getByRole('button', { name: addProductModel.formName }));

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

            fireEvent.change(screen.getByLabelText(/Price/i), {
                target: { value: updatedData.price.toString() }
            });

            fireEvent.click(screen.getByRole('button', { name: updateProductModel.formName }));

            await waitFor(() => {
                expect(mockOnSubmit).toHaveBeenCalled();
                expect(api.updateProduct).toHaveBeenCalledWith(existingProduct.id, updatedData);
                expect(window.alert).toHaveBeenCalledWith("update sản phẩm thành công");
            });
        });
    });

    describe("ProductManagementPage Integration Tests", () => {
        test("ProductList renders products from API", async () => {
            const apiProducts = [
                { id: 1, productName: "Laptop Dell", price: 15000000, quantity: 10 },
                { id: 2, productName: "Headphone Sony", price: 2000000, quantity: 5 },
            ];

            api.getProducts.mockResolvedValue({ data: { content: apiProducts, totalPages: 1 } });

            render(
                <MemoryRouter>
                    <ProductManagementPage />
                </MemoryRouter>
            );

            await waitFor(() => {
                apiProducts.forEach((p) => {
                    expect(screen.getByText(p.productName)).toBeInTheDocument();
                    expect(
                        screen.getByText((content) =>
                            content.replace(/[^\d]/g, "") === p.price.toString()
                        )
                    ).toBeInTheDocument();
                });
            });
        });

        test("renders empty ProductList when API returns no products", async () => {
            const emptyData = {
                data: { content: [], totalPages: 0 }
            };

            api.getProducts.mockResolvedValueOnce(emptyData);

            render(
                <MemoryRouter>
                    <ProductManagementPage />
                </MemoryRouter>
            );

            await waitFor(() => {
                expect(screen.getByText("Không có sản phẩm nào.")).toBeInTheDocument();
            });

            expect(api.getProducts).toHaveBeenCalled();
        });

        test("handles API error gracefully", async () => {
            api.getProducts.mockRejectedValueOnce(new Error("API Error"));

            render(
                <MemoryRouter>
                    <ProductManagementPage />
                </MemoryRouter>
            );

            await waitFor(() => {
                expect(screen.queryByText("Laptop Dell")).not.toBeInTheDocument();
            });

            expect(api.getProducts).toHaveBeenCalled();
        });
    });
});