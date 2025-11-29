
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
    // LoginPage.js
    usernameInput() {
        return cy.get('input[name="name"]');  // hoặc cy.get('#name') hoặc cy.get('input[placeholder="Enter Your Name"]')
    }

    passwordInput() {
        return cy.get('input[name="password"]');  // tương tự check HTML
    }

    submitButton() {
        return cy.get('button[type="submit"]');
    }
}

module.exports = new LoginPage();
