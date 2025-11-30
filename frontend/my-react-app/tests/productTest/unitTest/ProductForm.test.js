import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Form from '../../../src/components/Forms/Forms';
import { addProductModel } from '../../../src/models/addProductFormModel';
import { updateProductModel } from '../../../src/models/updateProductFormModel';

describe('Product Form Component Tests', () => {
    const mockOnSubmit = jest.fn();
    const mockToCloseForm = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
    });

    // ========== Test 1: Render form correctly ==========
    test('TC1: Render form với tất cả fields', () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
            />
        );

        expect(screen.getByLabelText(/Product Name/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Price/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Quantity/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Category/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: addProductModel.formName })).toBeInTheDocument();
    });

    // ========== Test 2: Submit form thành công ==========
    test('TC2: Submit form với dữ liệu hợp lệ', async () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
            />
        );

        // Fill form
        fireEvent.change(screen.getByLabelText(/Product Name/i), { 
            target: { value: 'iPhone 15' } 
        });
        fireEvent.change(screen.getByLabelText(/Price/i), { 
            target: { value: '25000000' } 
        });
        fireEvent.change(screen.getByLabelText(/Quantity/i), { 
            target: { value: '10' } 
        });
        fireEvent.change(screen.getByLabelText(/Description/i), { 
            target: { value: 'Flagship phone' } 
        });
        fireEvent.change(screen.getByLabelText(/Category/i), { 
            target: { value: 'SMARTPHONE' } 
        });

        // Submit
        fireEvent.click(screen.getByRole('button', { name: addProductModel.formName }));

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith({
                productName: 'iPhone 15',
                price: 25000000,
                quantity: 10,
                description: 'Flagship phone',
                category: 'SMARTPHONE'
            });
            expect(mockOnSubmit).toHaveBeenCalledTimes(1);
        });
    });

    // ========== Test 3: Validation errors ==========
    test('TC3: Hiển thị lỗi khi submit form rỗng', async () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
            />
        );

        // Submit without filling
        fireEvent.click(screen.getByRole('button', { name: addProductModel.formName }));

        await waitFor(() => {
            expect(mockOnSubmit).not.toHaveBeenCalled();
            // Check for error messages (adjust selectors based on your Input component)
            const errors = screen.getAllByText(/required/i);
            expect(errors.length).toBeGreaterThan(0);
        });
    });

    // ========== Test 4: Edit mode - Pre-fill data ==========
    test('TC4: Pre-fill dữ liệu khi edit product', () => {
        const existingProduct = {
            id: 1,
            productName: 'Laptop Dell',
            price: 15000000,
            quantity: 5,
            description: 'Old description',
            category: 'LAPTOPS'
        };

        render(
            <Form
                formModel={updateProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
                object={existingProduct}
            />
        );

        expect(screen.getByLabelText(/Product Name/i).value).toBe('Laptop Dell');
        expect(screen.getByLabelText(/Price/i).value).toBe('15000000');
        expect(screen.getByLabelText(/Quantity/i).value).toBe('5');
        expect(screen.getByText(/ID: 1/i)).toBeInTheDocument();
    });

    // ========== Test 5: Close form ==========
    test('TC5: Click close icon gọi toCloseForm', () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
                closeIconDisplay={true}
            />
        );

        const closeIcon = screen.getByAltText(/close icon/i);
        fireEvent.click(closeIcon);

        expect(mockToCloseForm).toHaveBeenCalledTimes(1);
    });

    // ========== Test 6: Field validation on blur ==========
    test('TC6: Hiển thị lỗi khi blur field không hợp lệ', async () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
            />
        );

        const nameInput = screen.getByLabelText(/Product Name/i);
        
        // Enter invalid data
        fireEvent.change(nameInput, { target: { value: 'AB' } }); // Too short
        fireEvent.blur(nameInput);

        await waitFor(() => {
            expect(screen.getByText(/3-100/i)).toBeInTheDocument();
        });
    });

    // ========== Test 7: Number type conversion ==========
    test('TC7: Convert price và quantity sang number khi submit', async () => {
        render(
            <Form
                formModel={addProductModel}
                onSubmit={mockOnSubmit}
                toCloseForm={mockToCloseForm}
            />
        );

        fireEvent.change(screen.getByLabelText(/Product Name/i), { 
            target: { value: 'Test' } 
        });
        fireEvent.change(screen.getByLabelText(/Price/i), { 
            target: { value: '1000' } 
        });
        fireEvent.change(screen.getByLabelText(/Quantity/i), { 
            target: { value: '5' } 
        });

        fireEvent.click(screen.getByRole('button'));

        await waitFor(() => {
            expect(mockOnSubmit).toHaveBeenCalledWith(
                expect.objectContaining({
                    price: 1000,      // Number, not string
                    quantity: 5       // Number, not string
                })
            );
        });
    });
});