import ProductPage from "../pages/ProductPage";

describe("Product E2E Test", () => {
    const page = new ProductPage();

    const mockProducts = [
        { id: 1, name: "ao", price: 100, quantity: 5, description: "ao dep" },
    ];

    beforeEach(() => {
        window.localStorage.setItem(
            "user",
            JSON.stringify({ id: 1, username: "mindang1", role: "admin" })
        );

        cy.intercept("GET", "/api/auth/current", {
            statusCode: 200,
            body: { id: 1, username: "mindang1", role: "admin" },
        }).as("getUser");

        cy.intercept("GET", "/api/products*", {
            statusCode: 200,
            body: { content: mockProducts, totalPages: 1 },
        }).as("getProducts");

        cy.intercept("POST", "/api/products", (req) => {
            req.reply({
                statusCode: 201,
                body: { ...req.body, id: 2 },
            });
        }).as("createProduct");

        cy.intercept("PUT", "/api/products/*", (req) => {
            req.reply({ statusCode: 200, body: req.body });
        }).as("updateProduct");

        cy.intercept("DELETE", "/api/products/*", { statusCode: 200 }).as("deleteProduct");
    });

    it("Hiển thị danh sách sản phẩm", () => {
        page.visit();
        cy.wait("@getUser");
        cy.wait("@getProducts");

        page.checkProductExists("ao").should("exist");
    });

    // it("Thêm sản phẩm mới thành công", () => {
    //     page.visit();
    //     cy.wait("@getUser");
    //     cy.wait("@getProducts");
    //
    //     page.clickAddNew();
    //     page.fillProductForm({ name: "iPhone 99", price: "9999", quantity: "10", description: "ok" });
    //     page.submitForm();
    //
    //     cy.wait("@createProduct");
    //     page.checkProductExists("iPhone 99").should("exist");
    // });

    // it("Sửa sản phẩm thành công", () => {
    //     page.visit();
    //     cy.wait("@getUser");
    //     cy.wait("@getProducts");
    //
    //     const updatedProduct = {
    //         name: "ao moi",
    //         price: "5000",
    //         quantity: "2",
    //         description: "ok"
    //     };
    //
    //     page.editProduct("ao", updatedProduct);
    //
    //     cy.contains('[data-testid="product-item"]', "ao moi").should("exist");
    // });

    it("Filter/Sort sản phẩm theo trường và chiều", () => {
        cy.visit("/products");

        // Giả lập API trả về danh sách sản phẩm
        cy.intercept("GET", "/api/products*", {
            statusCode: 200,
            body: {
                content: [
                    { id: 1, name: "iPhone 99", price: 9999, quantity: 10 },
                    { id: 2, name: "Samsung Galaxy", price: 8000, quantity: 5 },
                    { id: 3, name: "Xiaomi Note", price: 6000, quantity: 8 }
                ],
                totalPages: 1
            },
        }).as("getProducts");

        cy.wait("@getProducts");

        cy.get('[data-testid="sort-by"]').select("price");
        cy.get('[data-testid="sort-dir"]').select("desc");

        cy.get('[data-testid="product-item"]')
            .first()
            .should("contain.text", "iPhone 99");

        cy.get('[data-testid="product-item"]')
            .last()
            .should("contain.text", "Xiaomi Note");
    });

    it("Xóa sản phẩm thành công", () => {
        page.visit();
        cy.wait("@getUser");
        cy.wait("@getProducts");

        page.deleteProduct("ao");

        cy.wait("@deleteProduct");

        cy.intercept("GET", "/api/products*", {
            statusCode: 200,
            body: { content: [], totalPages: 0 },
        }).as("getProductsAfterDelete");

        cy.reload();
        cy.wait("@getProductsAfterDelete");

        page.checkProductExists("ao").should("not.exist");
    });
});
