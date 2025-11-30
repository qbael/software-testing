import { validateProduct } from '../../../src/utils/validations/productValidation'; // bạn phải có file này

describe('3.2.1 - Product Validation Unit Tests (TDD)', () => {

    test('TC1: Product name rỗng → trả về lỗi', () => {
        const product = { productName: '', price: 1000, quantity: 10 };
        const errors = validateProduct(product);
        expect(errors.productName).toBe('Tên sản phẩm phải từ 3-100 ký tự');
    });

    test('TC2: Product name < 3 ký tự → lỗi', () => {
        const product = { productName: 'AB', price: 1000, quantity: 10 };
        const errors = validateProduct(product);
        expect(errors.productName).toBe('Tên sản phẩm phải từ 3-100 ký tự');
    });

    test('TC3: Product name > 100 ký tự → lỗi', () => {
        const product = { productName: 'a'.repeat(101), price: 1000, quantity: 10 };
        const errors = validateProduct(product);
        expect(errors.productName).toBe('Tên sản phẩm phải từ 3-100 ký tự');
    });

    test('TC4: Price <= 0 → lỗi', () => {
        const product = { productName: 'Valid', price: 0, quantity: 10 };
        const errors = validateProduct(product);
        expect(errors.price).toBe('Giá phải lớn hơn 0');
    });

    test('TC5: Price âm → lỗi', () => {
        const product = { productName: 'Valid', price: -100, quantity: 10 };
        const errors = validateProduct(product);
        expect(errors.price).toBe('Giá phải lớn hơn 0');
    });

    test('TC6: Quantity âm → lỗi', () => {
        const product = { productName: 'Valid', price: 1000, quantity: -1 };
        const errors = validateProduct(product);
        expect(errors.quantity).toBe('Số lượng phải >= 0');
    });

    test('TC7: Description > 500 ký tự → lỗi', () => {
        const product = {
            productName: 'Valid',
            price: 1000,
            quantity: 10,
            description: 'a'.repeat(501)
        };
        const errors = validateProduct(product);
        expect(errors.description).toBe('Mô tả không được vượt quá 500 ký tự');
    });

    test('TC8: Category không hợp lệ → lỗi', () => {
        const product = {
            productName: 'Valid',
            price: 1000,
            quantity: 10,
            category: 'INVALID_CAT'
        };
        const errors = validateProduct(product);
        expect(errors.category).toBe('Danh mục không hợp lệ');
    });

    test('TC9: Product hợp lệ hoàn toàn → không lỗi', () => {
        const product = {
            productName: 'iPhone 15',
            price: 25000000,
            quantity: 50,
            description: 'Flagship 2024',
            category: 'SMARTPHONE'
        };
        const errors = validateProduct(product);
        expect(errors).toEqual({});
    });
});