class ProductPage {
    visit() {
        cy.visit('/products');
    }

    clickAddNew() {
        cy.get('[data-testid="add-product-btn"]').click();
    }

    fillProductForm(product) {
        if (product.productName) cy.get('[data-testid="product-input-productName"]').clear().type(product.productName);
        if (product.price) cy.get('[data-testid="product-input-price"]').clear().type(product.price);
        if (product.quantity) cy.get('[data-testid="product-input-quantity"]').clear().type(product.quantity);
        if (product.description) cy.get('[data-testid="product-input-description"]').clear().type(product.description);
        if (product.category) cy.get('[data-testid="product-input-category"]').select(product.category);
    }

    submitForm() {
        cy.get('[data-testid="submit-btn"]').click();
    }

    editProduct(name, updatedProduct) {
        cy.contains('[data-testid="product-item"]', name)
            .within(() => {
                cy.get('[data-testid="edit-btn"]').click();
            });

        if (updatedProduct){
            if (updatedProduct.productName) cy.get('[data-testid="product-input-productName"]').clear().type(updatedProduct.productName);
            if (updatedProduct.price) cy.get('[data-testid="product-input-price"]').clear().type(updatedProduct.price);
            if (updatedProduct.quantity) cy.get('[data-testid="product-input-quantity"]').clear().type(updatedProduct.quantity);
            if (updatedProduct.description) cy.get('[data-testid="product-input-description"]').clear().type(updatedProduct.description);
            if (updatedProduct.category) cy.get('[data-testid="product-input-category"]').select(updatedProduct.category);
        } else {
            cy.get('[data-testid="product-form"]').within(() => {
                cy.get('[data-testid="product-input-productName"]').clear().type('Tên mới');
                cy.get('[data-testid="product-input-price"]').clear().type('1000');
                cy.get('[data-testid="product-input-quantity"]').clear().type('10');
                cy.get('[data-testid="product-input-description"]').clear().type('Mô tả mới');
                cy.get('[data-testid="product-input-category"]').select("LAPTOPS");
            });
        }

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
