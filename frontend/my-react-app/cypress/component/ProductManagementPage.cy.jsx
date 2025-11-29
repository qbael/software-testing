import { mount } from '@cypress/react';
import ProductManagementPage from '../../src/pages/productPage/product.jsx';
import * as authApi from '../../src/api/authApi.js';
import * as productApi from '../../src/api/productAPI.js';

describe('ProductManagementPage Component Test', () => {

    beforeEach(() => {
        cy.stub(authApi, 'getCurrentUser').resolves({ id: 1, username: 'mindang1' });

        cy.stub(productApi, 'getProducts').resolves({
            data: {
                content: [],
                totalPages: 0
            }
        });

        cy.stub(productApi, 'createProduct').resolves({ id: 1, name: 'iPhone 99', price: 9999, quantity: 10 });
    });

    it('Hiển thị nút Add Product và mở form khi click', () => {
        mount(<ProductManagementPage />);

        cy.get('[data-testid="add-product-btn"]').should('be.visible');

        cy.get('[data-testid="add-product-btn"]').click();
        cy.get('form').should('exist');
        cy.contains('Thêm sản phẩm').should('exist');
    });

});
