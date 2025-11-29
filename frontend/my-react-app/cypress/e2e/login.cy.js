// cypress/e2e/login.cy.js
const loginPage = require("../pages/LoginPage");

describe("Login E2E Test", () => {

    const validUser = { username: "mindang1", password: "mindang1" };
    const invalidUser = { username: "wrong", password: "wrong" };
    const invalidUser2 = { username: "mindang1", password: "wrongpassword" };

    it("Login thành công", () => {
        loginPage.visit();
        cy.url().should('include', '/login');
        cy.get('body').should('be.visible');
        cy.get('input, form, button', { timeout: 15000 }).should('exist');
        loginPage.typeUsername(validUser.username);
        loginPage.typePassword(validUser.password);
        loginPage.clickLogin();

        cy.url().should("include", "/admin");
        cy.on('window:alert', (txt) => {
            expect(txt).to.contains("Đăng nhập thành công");
        });
    });

    it("Login thất bại - username sai", () => {
        loginPage.visit();
        loginPage.typeUsername(invalidUser.username);
        loginPage.typePassword(invalidUser.password);
        loginPage.clickLogin();

        cy.on('window:alert', (txt) => {
            expect(txt).to.contains("Username or password incorrect");
        });
    });

    it("Login thất bại - mật khẩu sai", () => {
        loginPage.visit();
        loginPage.typeUsername(invalidUser2.username);
        loginPage.typePassword(invalidUser2.password);
        loginPage.clickLogin();

        cy.on('window:alert', (txt) => {
            expect(txt).to.contains("Username or password incorrect");
        });
    });

    it("Username required validation", () => {
        loginPage.visit();
        loginPage.typePassword("123456");
        loginPage.clickLogin();

        cy.get('div._error_1el5m_26')
            .should('contain.text', 'This field is required');
    });

    it("Password required validation", () => {
        loginPage.visit();
        loginPage.typeUsername("mindang1");
        loginPage.clickLogin();

        cy.get('div._error_1el5m_26')
            .should('contain.text', 'This field is required');
    });

    it("UI elements interactions - simple", () => {
        loginPage.visit();
        loginPage.usernameInput().focus().should('be.focused');
        loginPage.passwordInput().focus().should('be.focused');
    });
});
