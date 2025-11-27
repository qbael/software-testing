import { validateFields } from '../../../src/utils/validations/validate.js';

describe('Validation Module - Login Tests', () => {

    // ============================================
    // a) Unit tests cho validateUsername() - 2 điểm
    // ============================================

    describe('validateUsername (fieldName: "name")', () => {
        const ERROR_MSG = 'Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"';

        // Test username rỗng
        test.each([
            ['', 'empty string'],
            ['   ', 'only whitespace']
        ])('should return error when username is %s', (username) => {
            const result = validateFields('name', username, true);
            expect(result).toBe('This field is required');
        });

        // Test username quá ngắn
        test.each([
            ['ab', '2 characters'],
            ['a1', '2 alphanumeric'],
            ['x', '1 character']
        ])('should return error when username is too short: %s', (username) => {
            const result = validateFields('name', username, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test username quá dài
        test.each([
            ['a'.repeat(51), '51 characters'],
            ['user' + '1'.repeat(47), 'exactly 51 chars']
        ])('should return error when username is too long: %s', (username) => {
            const result = validateFields('name', username, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test ký tự đặc biệt không hợp lệ
        test.each([
            ['user name', 'contains spaces'],
            ['user@123', 'contains @'],
            ['user#name', 'contains #'],
            ['user!123', 'contains !'],
            ['userên', 'contains Vietnamese chars']
        ])('should return error when username %s', (username) => {
            const result = validateFields('name', username, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test username hợp lệ
        test.each([
            ['username', 'letters only'],
            ['user123', 'with numbers'],
            ['user.name', 'with dot'],
            ['user-name', 'with hyphen'],
            ['user_name', 'with underscore'],
            ['user_123.test-name', 'mixed valid chars'],
            ['abc', 'min length (3 chars)'],
            ['a'.repeat(50), 'max length (50 chars)']
        ])('should return empty string for valid username: %s', (username) => {
            const result = validateFields('name', username, true);
            expect(result).toBe('');
        });
    });

    // ============================================
    // b) Unit tests cho validatePassword() - 2 điểm
    // ============================================

    describe('validatePassword (fieldName: "password")', () => {
        const ERROR_MSG = 'Password must be 6-100 characters long and contain at least one letter and one number';

        // Test password rỗng
        test.each([
            ['', 'empty string'],
            ['     ', 'only whitespace']
        ])('should return error when password is %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe('This field is required');
        });

        // Test password quá ngắn
        test.each([
            ['abc12', '5 characters'],
            ['a1b2c', '5 characters mixed'],
            ['a', '1 character']
        ])('should return error when password is too short: %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test password quá dài
        test.each([
            ['a1' + 'b'.repeat(99), '101 characters'],
            ['password1' + '2'.repeat(92), 'exactly 101 chars']
        ])('should return error when password is too long: %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test password thiếu chữ hoặc số
        test.each([
            ['123456', 'only numbers'],
            ['123456789012', 'only numbers (long)'],
            ['abcdef', 'only lowercase letters'],
            ['ABCDEF', 'only uppercase letters'],
            ['AbCdEf', 'only mixed case letters']
        ])('should return error when password contains %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test password có ký tự đặc biệt (không hợp lệ)
        test.each([
            ['abc123!@#', 'special chars'],
            ['abc 123', 'spaces'],
            ['pass@word1', 'symbols']
        ])('should return error when password contains %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe(ERROR_MSG);
        });

        // Test password hợp lệ
        test.each([
            ['password123', 'lowercase + numbers'],
            ['PASSWORD123', 'uppercase + numbers'],
            ['PassWord123', 'mixed case + numbers'],
            ['abc123', 'min length (6 chars)'],
            ['a1b2c3', 'exactly 6 chars'],
            ['a1' + 'b'.repeat(98), 'max length (100 chars)'],
            ['test123456', 'multiple numbers'],
            ['123password', 'starts with number'],
            ['pass123word', 'numbers in middle']
        ])('should return empty string for valid password: %s', (password) => {
            const result = validateFields('password', password, true);
            expect(result).toBe('');
        });
    });

    // ============================================
    // Additional Edge Cases for Coverage >= 90%
    // ============================================

    describe('Additional Edge Cases', () => {
        test.each([
            [null, 'null'],
            [undefined, 'undefined']
        ])('should handle %s value for required field', (value) => {
            const result = validateFields('name', value, true);
            expect(result).toBe('This field is required');
        });

        test('should return empty string for unknown field types', () => {
            const result = validateFields('unknownField', 'someValue', false);
            expect(result).toBe('');
        });
    });
});