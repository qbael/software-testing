/* eslint-disable no-undef */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import LoginPage from '../../../src/pages/formPages/LoginPage';

// ============================================
// MOCK SETUP SECTION
// ============================================

// 1. Mock useNavigate từ react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockNavigate,
}));

// 2. Mock API modules
// Chúng ta mock đường dẫn tương đối chính xác tới file api của dự án
jest.mock('../../../src/api/authApi.js', () => ({
    login: jest.fn(),
    getCurrentUser: jest.fn(),
}));

// Import mock functions để thiết lập giá trị trả về trong từng test case
import { login, getCurrentUser } from '../../../src/api/authApi';

// 3. Mock window.alert (Vì JSDOM không hỗ trợ alert)
// Kiểm tra nếu window.alert chưa được mock thì mới gán để an toàn
if (!window.alert._isMockFunction) {
    window.alert = jest.fn();
}

describe('Login Page Integration Tests', () => {

    // Reset tất cả các mock trước mỗi test case để đảm bảo tính độc lập
    beforeEach(() => {
        jest.clearAllMocks();
    });

    // ============================================
    // a) Test rendering và user interactions (2 điểm)
    // ============================================
    test('TC1: Should render login form correctly and allow typing', () => {
        render(<LoginPage />);

        // Tìm input dựa trên Label trong FormModel (Name / Password)
        // Sử dụng regex /.../i để không phân biệt hoa thường
        const nameInput = screen.getByLabelText(/Name/i);
        const passwordInput = screen.getByLabelText(/Password/i);

        // Tìm nút button. name của button là 'Đăng nhập'
        const submitBtn = screen.getByRole('button', { name: /Đăng nhập/i });

        // Assert: Các phần tử phải xuất hiện trên màn hình
        expect(nameInput).toBeInTheDocument();
        expect(passwordInput).toBeInTheDocument();
        expect(submitBtn).toBeInTheDocument();

        // Test User Interaction: Gõ vào ô input
        fireEvent.change(nameInput, { target: { value: 'user123' } });
        fireEvent.change(passwordInput, { target: { value: 'Pass123' } });

        // Assert: Giá trị input phải thay đổi
        expect(nameInput.value).toBe('user123');
        expect(passwordInput.value).toBe('Pass123');
    });

    // ============================================
    // c) Test error handling (Validation Client-side) (1 điểm)
    // ============================================
    // (Lưu ý: Test validation được đưa lên trước test flow API để kiểm tra chặn lỗi trước)
    test('TC2: Validation Error - Should not call API if empty', async () => {
        render(<LoginPage />);

        // Click submit ngay mà không nhập gì cả
        fireEvent.click(screen.getByRole('button', { name: /Đăng nhập/i }));

        // Assert: Đảm bảo hàm login API KHÔNG được gọi
        expect(login).not.toHaveBeenCalled();

        // Assert: Kiểm tra UI hiển thị lỗi validation
        await waitFor(() => {
            // Tìm tất cả các text báo lỗi "This field is required" (từ validate.js)
            const errorMessages = screen.getAllByText(/This field is required/i);
            // Phải có ít nhất 1 lỗi xuất hiện
            expect(errorMessages.length).toBeGreaterThan(0);
        });
    });

    // ============================================
    // b) Test form submission và API calls (2 điểm)
    // ============================================
    test('TC3: Happy Path - Login success and navigate to admin', async () => {
        // Setup API mock trả về thành công
        login.mockResolvedValue({ success: true });
        getCurrentUser.mockResolvedValue({ id: 1, username: 'testuser' });

        render(<LoginPage />);

        // 1. Điền thông tin vào form
        const nameInput = screen.getByLabelText(/Name/i);
        const passwordInput = screen.getByLabelText(/Password/i);

        fireEvent.change(nameInput, { target: { value: 'testuser' } });
        fireEvent.change(passwordInput, { target: { value: 'Test123' } });

        // 2. Submit form
        fireEvent.click(screen.getByRole('button', { name: /Đăng nhập/i }));

        // 3. Assert API calls & Navigation
        await waitFor(() => {
            // Kiểm tra API login được gọi đúng tham số
            expect(login).toHaveBeenCalledWith('testuser', 'Test123');

            // Kiểm tra API lấy user info được gọi
            expect(getCurrentUser).toHaveBeenCalled();

            // Kiểm tra thông báo thành công
            expect(window.alert).toHaveBeenCalledWith('Đăng nhập thành công!');

            // Kiểm tra chuyển hướng sang trang admin
            expect(mockNavigate).toHaveBeenCalledWith('/admin');
        });
    });

    // ============================================
    // c) Test error handling (API Error Server-side) (tiếp theo)
    // ============================================
    test('TC4: API Error - Should show alert on login failure', async () => {
        // Setup API mock trả về lỗi
        const errorMsg = 'Sai mật khẩu hoặc tên đăng nhập';
        const errorResponse = {
            response: { data: errorMsg }
        };
        login.mockRejectedValue(errorResponse);

        render(<LoginPage />);

        // Điền form (dù sai pass)
        fireEvent.change(screen.getByLabelText(/Name/i), { target: { value: 'wronguser' } });
        fireEvent.change(screen.getByLabelText(/Password/i), { target: { value: 'wrongpass1' } });

        // Submit
        fireEvent.click(screen.getByRole('button', { name: /Đăng nhập/i }));

        // Assert
        await waitFor(() => {
            // Hàm login vẫn được gọi
            expect(login).toHaveBeenCalled();

            // Alert hiển thị đúng thông báo lỗi từ mock data
            expect(window.alert).toHaveBeenCalledWith(errorMsg);

            // Không được chuyển trang
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });
});