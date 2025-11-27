import { validateFields } from '../../../src/utils/validations/validate.js';

describe('Validation Module - Login Tests', () => {

    // ============================================
    // a) Unit tests cho validateUsername() - 2 điểm
    // ============================================

    describe('validateUsername (fieldName: "name")', () => {

        // Test username rỗng
        test('should return error when username is empty', () => {
            const result = validateFields('name', '', true);
            expect(result).toBe('This field is required');
        });

        test('should return error when username is only whitespace', () => {
            const result = validateFields('name', '   ', true);
            expect(result).toBe('This field is required');
        });

        // Test username quá ngắn
        test('should return error when username is too short (less than 3 characters)', () => {
            const result = validateFields('name', 'ab', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username has only 2 characters', () => {
            const result = validateFields('name', 'a1', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        // Test username quá dài
        test('should return error when username is too long (more than 50 characters)', () => {
            const longUsername = 'a'.repeat(51);
            const result = validateFields('name', longUsername, true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username has exactly 51 characters', () => {
            const longUsername = 'user' + '1'.repeat(47);
            const result = validateFields('name', longUsername, true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        // Test ký tự đặc biệt không hợp lệ
        test('should return error when username contains spaces', () => {
            const result = validateFields('name', 'user name', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username contains special characters (@)', () => {
            const result = validateFields('name', 'user@123', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username contains special characters (#)', () => {
            const result = validateFields('name', 'user#name', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username contains special characters (!)', () => {
            const result = validateFields('name', 'user!123', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return error when username contains Vietnamese characters', () => {
            const result = validateFields('name', 'userên', true);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        // Test username hợp lệ
        test('should return empty string for valid username with letters only', () => {
            const result = validateFields('name', 'username', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid username with numbers', () => {
            const result = validateFields('name', 'user123', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid username with dot', () => {
            const result = validateFields('name', 'user.name', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid username with hyphen', () => {
            const result = validateFields('name', 'user-name', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid username with underscore', () => {
            const result = validateFields('name', 'user_name', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid username with mixed valid characters', () => {
            const result = validateFields('name', 'user_123.test-name', true);
            expect(result).toBe('');
        });

        test('should return empty string for minimum valid length (3 characters)', () => {
            const result = validateFields('name', 'abc', true);
            expect(result).toBe('');
        });

        test('should return empty string for maximum valid length (50 characters)', () => {
            const validUsername = 'a'.repeat(50);
            const result = validateFields('name', validUsername, true);
            expect(result).toBe('');
        });

        // Test when field is not required but still validates format if value exists
        test('should still validate format even when field is not required', () => {
            // When not required but has invalid format, still returns error
            const result = validateFields('name', 'ab', false);
            expect(result).toBe('Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"');
        });

        test('should return empty string when field is not required and value is valid', () => {
            const result = validateFields('name', 'validuser', false);
            expect(result).toBe('');
        });
    });


    // ============================================
    // b) Unit tests cho validatePassword() - 2 điểm
    // ============================================

    describe('validatePassword (fieldName: "password")', () => {

        // Test password rỗng
        test('should return error when password is empty', () => {
            const result = validateFields('password', '', true);
            expect(result).toBe('This field is required');
        });

        test('should return error when password is only whitespace', () => {
            const result = validateFields('password', '     ', true);
            expect(result).toBe('This field is required');
        });

        // Test password quá ngắn
        test('should return error when password is too short (less than 6 characters)', () => {
            const result = validateFields('password', 'abc12', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password has only 5 characters', () => {
            const result = validateFields('password', 'a1b2c', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password has only 1 character', () => {
            const result = validateFields('password', 'a', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        // Test password quá dài
        test('should return error when password is too long (more than 100 characters)', () => {
            const longPassword = 'a1' + 'b'.repeat(99);
            const result = validateFields('password', longPassword, true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password has exactly 101 characters', () => {
            const longPassword = 'password1' + '2'.repeat(92);
            const result = validateFields('password', longPassword, true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        // Test password không có chữ
        test('should return error when password contains only numbers', () => {
            const result = validateFields('password', '123456', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password contains only numbers (long)', () => {
            const result = validateFields('password', '123456789012', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        // Test password không có số
        test('should return error when password contains only letters', () => {
            const result = validateFields('password', 'abcdef', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password contains only letters (uppercase)', () => {
            const result = validateFields('password', 'ABCDEF', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password contains only letters (mixed case)', () => {
            const result = validateFields('password', 'AbCdEf', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        // Test password có ký tự đặc biệt (không hợp lệ)
        test('should return error when password contains special characters', () => {
            const result = validateFields('password', 'abc123!@#', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password contains spaces', () => {
            const result = validateFields('password', 'abc 123', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return error when password contains symbols', () => {
            const result = validateFields('password', 'pass@word1', true);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        // Test password hợp lệ
        test('should return empty string for valid password with lowercase letters and numbers', () => {
            const result = validateFields('password', 'password123', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password with uppercase letters and numbers', () => {
            const result = validateFields('password', 'PASSWORD123', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password with mixed case letters and numbers', () => {
            const result = validateFields('password', 'PassWord123', true);
            expect(result).toBe('');
        });

        test('should return empty string for minimum valid length (6 characters)', () => {
            const result = validateFields('password', 'abc123', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password with exactly 6 characters', () => {
            const result = validateFields('password', 'a1b2c3', true);
            expect(result).toBe('');
        });

        test('should return empty string for maximum valid length (100 characters)', () => {
            const validPassword = 'a1' + 'b'.repeat(98);
            const result = validateFields('password', validPassword, true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password with multiple numbers', () => {
            const result = validateFields('password', 'test123456', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password starting with number', () => {
            const result = validateFields('password', '123password', true);
            expect(result).toBe('');
        });

        test('should return empty string for valid password with numbers in middle', () => {
            const result = validateFields('password', 'pass123word', true);
            expect(result).toBe('');
        });

        // Test when field is not required but still validates format if value exists
        test('should still validate format even when field is not required', () => {
            // When not required but has invalid format, still returns error
            const result = validateFields('password', 'abc', false);
            expect(result).toBe('Password must be 6-100 characters long and contain at least one letter and one number');
        });

        test('should return empty string when field is not required and value is valid', () => {
            const result = validateFields('password', 'valid123', false);
            expect(result).toBe('');
        });
    });


    // ============================================
    // Additional Edge Cases for Coverage >= 90%
    // ============================================

    describe('Additional Edge Cases', () => {

        test('should handle null value for required field', () => {
            const result = validateFields('name', null, true);
            expect(result).toBe('This field is required');
        });

        test('should handle undefined value for required field', () => {
            const result = validateFields('name', undefined, true);
            expect(result).toBe('This field is required');
        });

        test('should return empty string for other field types not in switch case', () => {
            const result = validateFields('unknownField', 'someValue', false);
            expect(result).toBe('');
        });

        test('should validate email field correctly', () => {
            const validEmail = validateFields('email', 'test@example.com', true);
            expect(validEmail).toBe('');

            const invalidEmail = validateFields('email', 'invalid-email', true);
            expect(invalidEmail).toBe('Invalid email. Example: MinhAnfe23@gmail.com');
        });

        test('should validate phoneNumber field correctly', () => {
            const validPhone = validateFields('phoneNumber', '0123456789', true);
            expect(validPhone).toBe('');

            const invalidPhone = validateFields('phoneNumber', '123', true);
            expect(invalidPhone).toBe('Phone Number must have at least 10 digits');
        });

        test('should validate price field correctly', () => {
            const validPrice = validateFields('price', '100.50', true);
            expect(validPrice).toBe('');

            const invalidPrice = validateFields('price', 'abc', true);
            expect(invalidPrice).toBe('Price should have digits only');
        });
    });
});