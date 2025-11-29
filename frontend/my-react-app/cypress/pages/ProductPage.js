class ProductPage {
    visit() {
        cy.visit('/products'); // hoặc /admin nếu trang quản lý
    }

    // Button Add Product
    clickAddNew() {
        cy.get('[data-testid="add-product-btn"]').click();
    }

    // Form thao tác
    fillProductForm(product) {
        if (product.name) cy.get('[data-testid="product-name"]').clear().type(product.name);
        if (product.price) cy.get('[data-testid="product-price"]').clear().type(product.price);
        if (product.quantity) cy.get('[data-testid="product-quantity"]').clear().type(product.quantity);
        if (product.description) cy.get('[data-testid="product-description"]').clear().type(product.description);
    }

    submitForm() {
        cy.get('[data-testid="submit-btn"]').click();
    }

    // Các thao tác với sản phẩm trong list
    // ProductPage.js
    editProduct(name, updatedProduct) {
        cy.contains('[data-testid="product-item"]', name)
            .within(() => {
                cy.get('[data-testid="edit-btn"]').click(); // mở form edit
            });

        // Clear và điền lại input trong form
        cy.get('[data-testid="product-form"]').within(() => {
            cy.get('[data-testid="product-name"]').clear().type('Tên mới');
            cy.get('[data-testid="product-price"]').clear().type('1000');
            cy.get('[data-testid="product-quantity"]').clear().type('10');
            cy.get('[data-testid="product-description"]').clear().type('Mô tả mới');
        });

        cy.get('[data-testid="submit-btn"]').click();
    }

    deleteProduct(name) {
        cy.contains('[data-testid="product-item"]', name)
            .find('[data-testid="delete-btn"]')
            .click();
    }


    checkProductExists(name) {
        return cy.contains('[data-testid="product-item"]', name);
    }
}

export default ProductPage;
