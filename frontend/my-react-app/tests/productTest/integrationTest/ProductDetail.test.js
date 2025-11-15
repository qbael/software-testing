import React, { useState } from "react";
import { render, screen, fireEvent, waitFor,within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProductList from "../../../src/pages/productPage/productList.jsx";
import InfoBoard from "../../../src/components/InfoBoard/InfoBoard.jsx";

describe("ProductDetail Integration Test", () => {
    const products = [
        { id: 1, productName: "Laptop Dell", price: 15000000, quantity: 10, description: "Mỏng nhẹ", category: "LAPTOPS" },
        { id: 2, productName: "Mouse Logitech", price: 200000, quantity: 50, description: "Chuột gaming", category: "HEADPHONES" }
    ];

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
        const icon = screen.getAllByAltText("icon")[0]; // lấy icon của sản phẩm đầu tiên
        fireEvent.click(icon);

        // Kiểm tra InfoBoard hiển thị đúng thông tin
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

        // Mở InfoBoard
        const icon = screen.getAllByAltText("icon")[0];
        fireEvent.click(icon); // hoặc userEvent.click(icon)

        // Kiểm tra InfoBoard đã hiện
        const board = screen.getByTestId("productDetail");
        expect(board).toBeInTheDocument();

        // Click ngoài overlay
        fireEvent.mouseDown(document.body); // dùng fireEvent cũng được

        // Kiểm tra InfoBoard đã đóng
        expect(screen.queryByTestId("productDetail")).not.toBeInTheDocument();
    });

});

