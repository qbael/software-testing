// jest.config.js
module.exports = {
    testEnvironment: "jsdom", // giả lập DOM
    setupFilesAfterEnv: ["<rootDir>/jest.setup.js"], // chạy sau khi môi trường được tạo

    // map các file không phải JS (CSS, module.css, ảnh, SVG)
    moduleNameMapper: {
        "\\.(css|less|scss|sass)$": "<rootDir>/__mocks__/fileMock.js",
        "\\.(jpg|jpeg|png|gif|webp|avif|svg)$": "<rootDir>/__mocks__/fileMock.js",
    },

    // transform để hỗ trợ JSX/ES6 (React)
    transform: {
        "^.+\\.(js|jsx|ts|tsx)$": "babel-jest",
    },

    // Jest sẽ bỏ qua node_modules trừ khi bạn muốn transform
    transformIgnorePatterns: ["/node_modules/"],

    moduleFileExtensions: ["js", "jsx", "json", "node"],
};