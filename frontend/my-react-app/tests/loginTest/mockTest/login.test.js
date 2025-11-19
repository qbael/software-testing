// LoginPage.test.jsx
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
        // Clear all mocks trước mỗi test
        jest.clearAllMocks();
    });

    // ============================================
    // a) Mock authService.loginUser() (1 điểm)
    // ============================================
    describe('Mock authApi functions', () => {
        test('should mock login function successfully', () => {
            // Arrange: Setup mock implementation
            const mockLoginResponse = { id: '123', username: 'testuser' };
            authApi.login.mockResolvedValue(mockLoginResponse);

            // Assert: Verify mock được setup đúng
            expect(authApi.login).toBeDefined();
            expect(typeof authApi.login).toBe('function');
        });

        test('should mock getCurrentUser function successfully', () => {
            // Arrange: Setup mock implementation
            const mockUserResponse = { id: '123', username: 'testuser' };
            authApi.getCurrentUser.mockResolvedValue(mockUserResponse);

            // Assert: Verify mock được setup đúng
            expect(authApi.getCurrentUser).toBeDefined();
            expect(typeof authApi.getCurrentUser).toBe('function');
        });
    });

    // ============================================
    // b) Test với mocked successful/failed responses (1 điểm)
    // ============================================
    describe('Successful login scenarios', () => {
        test('should handle successful login with valid credentials', async () => {
            // Arrange
            const mockLoginResponse = { id: '123-456', username: 'validuser' };
            const mockCurrentUser = { id: '123-456', username: 'validuser' };

            authApi.login.mockResolvedValue(mockLoginResponse);
            authApi.getCurrentUser.mockResolvedValue(mockCurrentUser);

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act: Fill form và submit (dùng exact label text từ HTML)
            const nameInput = screen.getByLabelText(/^Name/i);
            const passwordInput = screen.getByLabelText(/^Password/i);
            const submitButton = screen.getByRole('button', { name: /đăng nhập/i });

            await user.type(nameInput, 'validuser');
            await user.type(passwordInput, 'ValidPass123');
            await user.click(submitButton);

            // Assert: Verify successful flow
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledWith('validuser', 'ValidPass123');
                expect(authApi.getCurrentUser).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Đăng nhập thành công!');
                expect(mockNavigate).toHaveBeenCalledWith('/admin');
            });
        });

        test('should call login API with correct parameters', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '999', username: 'admin' });
            authApi.getCurrentUser.mockResolvedValue({ id: '999', username: 'admin' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'admin');
            await user.type(screen.getByLabelText(/^Password/i), 'Admin@123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify API được gọi với đúng tham số
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.login).toHaveBeenCalledWith('admin', 'Admin@123');
            });
        });
    });

    describe('Failed login scenarios', () => {
        test('should handle user not found error (404)', async () => {
            // Arrange: Mock failed response - user not found
            const errorResponse = {
                response: {
                    status: 404,
                    data: 'Không tìm thấy người dùng'
                }
            };
            authApi.login.mockRejectedValue(errorResponse);

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'nonexistentuser');
            await user.type(screen.getByLabelText(/^Password/i), 'SomePass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify error handling
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Không tìm thấy người dùng');
                expect(mockNavigate).not.toHaveBeenCalled();
            });
        });

        test('should handle wrong password error (401)', async () => {
            // Arrange: Mock failed response - wrong password
            const errorResponse = {
                response: {
                    status: 401,
                    data: 'Sai mật khẩu'
                }
            };
            authApi.login.mockRejectedValue(errorResponse);

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'validuser');
            await user.type(screen.getByLabelText(/^Password/i), 'WrongPassword');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Sai mật khẩu');
                expect(mockNavigate).not.toHaveBeenCalled();
            });
        });

        test('should handle network error with default message', async () => {
            // Arrange: Mock network error (no response)
            const errorResponse = {
                message: 'Network Error'
            };
            authApi.login.mockRejectedValue(errorResponse);

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'testuser');
            await user.type(screen.getByLabelText(/^Password/i), 'Pass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Should show default error message
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalled();
                expect(global.alert).toHaveBeenCalledWith('Đăng ký thất bại. Vui lòng thử lại.');
            });
        });

        test('should handle server error (500)', async () => {
            // Arrange
            const errorResponse = {
                response: {
                    status: 500,
                    data: 'Internal Server Error'
                }
            };
            authApi.login.mockRejectedValue(errorResponse);

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'user');
            await user.type(screen.getByLabelText(/^Password/i), 'Pass');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert
            await waitFor(() => {
                expect(global.alert).toHaveBeenCalledWith('Internal Server Error');
            });
        });
    });

    // ============================================
    // c) Verify mock calls (0.5 điểm)
    // ============================================
    describe('Verify mock function calls', () => {
        test('should verify login was called exactly once', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '1', username: 'user' });
            authApi.getCurrentUser.mockResolvedValue({ id: '1', username: 'user' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'user');
            await user.type(screen.getByLabelText(/^Password/i), 'password');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify số lần gọi
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
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

            // Assert: Verify call sequence
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.getCurrentUser).toHaveBeenCalledTimes(1);
            });
        });

        test('should verify mock call arguments are correct', async () => {
            // Arrange
            const expectedUsername = 'testuser123';
            const expectedPassword = 'SecurePass456!';

            authApi.login.mockResolvedValue({ id: '3', username: expectedUsername });
            authApi.getCurrentUser.mockResolvedValue({ id: '3', username: expectedUsername });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), expectedUsername);
            await user.type(screen.getByLabelText(/^Password/i), expectedPassword);
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify với đúng arguments
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledWith(expectedUsername, expectedPassword);
                expect(authApi.login.mock.calls[0][0]).toBe(expectedUsername);
                expect(authApi.login.mock.calls[0][1]).toBe(expectedPassword);
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
            await user.type(screen.getByLabelText(/^Password/i), 'wrongpass');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: getCurrentUser should NOT be called on login failure
            await waitFor(() => {
                expect(authApi.login).toHaveBeenCalledTimes(1);
                expect(authApi.getCurrentUser).not.toHaveBeenCalled();
            });
        });

        test('should verify navigation is called with correct route', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '4', username: 'nav-test' });
            authApi.getCurrentUser.mockResolvedValue({ id: '4', username: 'nav-test' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'nav-test');
            await user.type(screen.getByLabelText(/^Password/i), 'pass123');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify navigate được gọi với '/admin'
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledTimes(1);
                expect(mockNavigate).toHaveBeenCalledWith('/admin');
            });
        });

        test('should verify alert is called with success message', async () => {
            // Arrange
            authApi.login.mockResolvedValue({ id: '5', username: 'alert-test' });
            authApi.getCurrentUser.mockResolvedValue({ id: '5', username: 'alert-test' });

            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act
            await user.type(screen.getByLabelText(/^Name/i), 'alert-test');
            await user.type(screen.getByLabelText(/^Password/i), 'pass456');
            await user.click(screen.getByRole('button', { name: /đăng nhập/i }));

            // Assert: Verify alert message
            await waitFor(() => {
                expect(global.alert).toHaveBeenCalledTimes(1);
                expect(global.alert).toHaveBeenCalledWith('Đăng nhập thành công!');
            });
        });

        test('should verify mock was reset between tests', () => {
            // Assert: Mocks should be clean (đã clear trong beforeEach)
            expect(authApi.login).not.toHaveBeenCalled();
            expect(authApi.getCurrentUser).not.toHaveBeenCalled();
            expect(mockNavigate).not.toHaveBeenCalled();
            expect(global.alert).not.toHaveBeenCalled();
        });
    });

    // Bonus: Test back button navigation
    describe('Navigation tests', () => {
        test('should navigate back when back button is clicked', async () => {
            // Arrange
            const user = userEvent.setup();
            renderWithRouter(<LoginPage />);

            // Act: Click back button
            const backButton = screen.getByAltText(/comeback icon/i);
            await user.click(backButton);

            // Assert
            expect(mockNavigate).toHaveBeenCalledWith('/');
        });
    });
});