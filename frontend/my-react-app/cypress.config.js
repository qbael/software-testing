const { defineConfig } = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:3000',
        setupNodeEvents(on, config) {
            require('cypress-mochawesome-reporter/plugin')(on);
            return config;
        },
        specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}', // Định nghĩa file E2E
    },
    component: {
        devServer: {
            framework: 'react',
            bundler: 'webpack',
        },
        specPattern: 'cypress/component/**/*.cy.{js,jsx,ts,tsx}', // File Component Test
    },
    reporter: 'cypress-mochawesome-reporter',
    reporterOptions: {
        reportDir: 'cypress/results',
        charts: true,
        reportPageTitle: 'Cypress Test Report',
        embeddedScreenshots: true,
        overwrite: false,
        html: true,
        json: true,
    },
});
