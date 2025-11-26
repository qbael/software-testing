import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import ProductManagementPage from "../../../src/pages/productPage/product.jsx";
import * as api from "../../../src/api/productAPI.js";
import * as authApi from "../../../src/api/authApi.js";


jest.mock("../../../src/api/authApi.js");
jest.mock("../../../src/api/productAPI.js");
describe("Integration Test: ProductManagementPage with API → ProductList", () => {

    beforeEach(() => {
        // Mock current user
        authApi.getCurrentUser.mockResolvedValue({ username: "admin" });
        authApi.logout.mockResolvedValue();
    });

    test("ProductList renders products from API", async () => {
        const products = [
            { id: 1, productName: "Laptop Dell", price: 15000000, quantity: 10 },
            { id: 2, productName: "Headphone Sony", price: 2000000, quantity: 5 },
        ];

        api.getProducts.mockResolvedValue({ data: { content: products, totalPages: 1 } });

        render(
            <MemoryRouter>
                <ProductManagementPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            products.forEach((p) => {
                // Tên thì ok
                expect(screen.getByText(p.productName)).toBeInTheDocument();
                // Giá dùng hàm, bỏ dấu phẩy/định dạng
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

        render( <MemoryRouter>
            <ProductManagementPage />
        </MemoryRouter>);

        await waitFor(() => {
            expect(screen.getByText("Không có sản phẩm nào.")).toBeInTheDocument();
        });

        expect(api.getProducts).toHaveBeenCalled();
    });

    test("handles API error gracefully", async () => {
        api.getProducts.mockRejectedValueOnce(new Error("API Error"));

        render( <MemoryRouter>
            <ProductManagementPage />
        </MemoryRouter>);

        await waitFor(() => {
            // Kiểm tra console error hoặc thông báo UI (nếu bạn muốn)
            expect(screen.queryByText("Laptop Dell")).not.toBeInTheDocument();
        });

        expect(api.getProducts).toHaveBeenCalled();
    });

});
