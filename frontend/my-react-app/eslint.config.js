// .eslintrc.js
import js from '@eslint/js';
import globals from 'globals';
import { defineConfig, globalIgnores } from 'eslint/config';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefreshPlugin from 'eslint-plugin-react-refresh';
import cypressPlugin from 'eslint-plugin-cypress';

export default defineConfig([
    // Bỏ qua thư mục build
    globalIgnores(['dist']),

    {
        files: ['**/*.{js,jsx}'], // Áp dụng cho JS/JSX
        extends: [
            js.configs.recommended,                   // ESLint khuyến nghị
            reactHooks.configs['recommended-latest'], // React Hooks
            reactRefreshPlugin.configs.vite,          // Vite + React Refresh
        ],
        plugins: ['cypress'],
        languageOptions: {
            ecmaVersion: 2020,
            globals: {
                ...globals.browser, // biến global trình duyệt
                ...globals.node,    // biến global Node.js (require, module, process,...)
                // Cypress globals
                cy: 'readonly',
                describe: 'readonly',
                it: 'readonly',
                before: 'readonly',
                after: 'readonly',
            },
            parserOptions: {
                ecmaVersion: 'latest',
                sourceType: 'module',
                ecmaFeatures: {
                    jsx: true,
                },
            },
        },
        rules: {
            'no-unused-vars': [
                'error',
                {
                    varsIgnorePattern: '^[A-Z_]', // bỏ qua biến PascalCase (class)
                    argsIgnorePattern: '^_',      // bỏ qua tham số function chưa dùng bắt đầu bằng _
                },
            ],
            'no-undef': 'error', // vẫn kiểm tra biến undefined
        },
    },
]);
