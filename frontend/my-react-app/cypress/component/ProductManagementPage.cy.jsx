import { mount } from '@cypress/react';
import ProductManagementPage from '../../../src/pages/products/ProductManagementPage';
import * as authApi from '../../../src/api/authApi';
import * as productApi from '../../../src/api/productAPI';

describe('ProductManagementPage Component Test', () => {

    beforeEach(() => {
        // Mock API getCurrentUser
        cy.stub(authApi, 'getCurrentUser').resolves({ id: 1, username: 'mindang1' });

        // Mock API getProducts
        cy.stub(productApi, 'getProducts').resolves({
            data: {
                content: [],
                totalPages: 0
            }
        });

        // Mock API createProduct
        cy.stub(productApi, 'createProduct').resolves({ id: 1, name: 'iPhone 99', price: 9999, quantity: 10 });
    });

    it('Hiển thị nút Add Product và mở form khi click', () => {
        mount(<ProductManagementPage />);

        // Kiểm tra nút Add Product hiện ra
        cy.get('[data-testid="add-product-btn"]').should('be.visible');

        // Click nút, form phải xuất hiện
        cy.get('[data-testid="add-product-btn"]').click();
        cy.get('form').should('exist');
        cy.contains('Thêm sản phẩm').should('exist');
    });

});
