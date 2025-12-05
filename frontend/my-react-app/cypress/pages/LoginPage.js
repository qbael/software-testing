
class LoginPage {
    visit() {
        cy.visit("/login");
    }

    typeUsername(username) {
        cy.get('input[name="name"]').clear().type(username);
    }

    typePassword(password) {
        cy.get('input[name="password"]').clear().type(password);
    }

    clickLogin() {
        cy.get('button[type="submit"]').click();
    }

    usernameInput() {
        return cy.get('input[name="name"]');
    }

    passwordInput() {
        return cy.get('input[name="password"]');
    }

    submitButton() {
        return cy.get('button[type="submit"]');
    }
}

module.exports = new LoginPage();
