import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../../../src/pages/formPages/LoginPage.jsx';
import * as authApi from '../../../src/api/authApi.js';

// Mock react-router-dom
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockNavigate,
}));

// Mock authApi module
jest.mock('../../../src/api/authApi.js');

// Mock alert
global.alert = jest.fn();

// Helper function để render component với Router
const renderWithRouter = (component) => {
    return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('LoginPage Component - Frontend Mocking Tests', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    // ============================================
    // a) Mock authApi.login() (1 điểm)
    // ============================================
    describe('Mock authApi functions', () => {
        test('should mock login and getCurrentUser functions successfully', () => {
            const mockLoginResponse = { id: '123', username: 'testuser' };
            const mockUserResponse = { id: '123', username: 'testuser' };

            authApi.login.mockResolvedValue(mockLoginResponse);
            authApi.getCurrentUser.mockResolvedValue(mockUserResponse);

            expect(authApi.login).toBeDefined();
            expect(authApi.getCurrentUser).toBeDefined();
        });
    });

    // ============================================
    // b) Test với mocked successful/failed responses (1 điểm)
    // ============================================
    describe('Login with mocked responses', () => {
        test('should handle successful login', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '123', username: 'validuser' });
            authApi.getCurrentUser.mockResolvedValue({ id: '123', username: 'validuser' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'validuser');
            await user.type(screen.getByLabelText(/^Password/i), 'ValidPass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledWith('validuser', 'ValidPass123');
                expect(authApi.getCurrentUser).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Đăng nhập thành công!');
                expect(mockNavigate).toHaveBeenCalledWith('/admin');
            });
        });

        test('should handle user not found error (404)', async () => {
            // Arrange
            authApi.login.mockRejectedValue({
                response: { status: 404, data: 'Không tìm thấy người dùng' }
            });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'wronguser');
            await user.type(screen.getByLabelText(/^Password/i), 'Pass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Không tìm thấy người dùng');
                expect(mockNavigate).not.toHaveBeenCalled();
            });
        });

        test('should handle wrong password error (401)', async () => {
            // Arrange
            authApi.login.mockRejectedValue({
                response: { status: 401, data: 'Sai mật khẩu' }
            });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'validuser');
            await user.type(screen.getByLabelText(/^Password/i), 'WrongPass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(global.alert).toHaveBeenCalledWith('Sai mật khẩu');
                expect(authApi.getCurrentUser).not.toHaveBeenCalled();
            });
        });

        test('should handle network error', async () => {
            // Arrange
            authApi.login.mockRejectedValue({ message: 'Network Error' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'testuser');
            await user.type(screen.getByLabelText(/^Password/i), 'Pass1234');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(global.alert).toHaveBeenCalledWith('Đăng nhập thất bại. Vui lòng thử lại.');
            });
        });
    });

    // ============================================
    // c) Verify mock calls (0.5 điểm)
    // ============================================
    describe('Verify mock function calls', () => {
        test('should verify login is called exactly once with correct arguments', async () => {
            // Arrange
            const username = 'testuser';
            const password = 'Test123';

            authApi.login.mockResolvedValue({ id: '1', username });
            authApi.getCurrentUser.mockResolvedValue({ id: '1', username });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), username);
            await user.type(screen.getByLabelText(/^Password/i), password);
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.login).toHaveBeenCalledWith(username, password);
            });
        });

        test('should verify getCurrentUser is called after successful login', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '2', username: 'admin' });
            authApi.getCurrentUser.mockResolvedValue({ id: '2', username: 'admin' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'admin');
            await user.type(screen.getByLabelText(/^Password/i), 'admin123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.getCurrentUser).toHaveBeenCalledTimes(1);
            });
        });

        test('should verify getCurrentUser is NOT called when login fails', async () => {
            // Arrange
            authApi.login.mockRejectedValue({
                response: { status: 401, data: 'Unauthorized' }
            });
            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'wronguser');
            await user.type(screen.getByLabelText(/^Password/i), 'wrong123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.getCurrentUser).not.toHaveBeenCalled();
            });
        });
    });
});