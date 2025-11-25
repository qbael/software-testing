const { defineConfig } = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:3000',
        setupNodeEvents(on, config) {
            require('cypress-mochawesome-reporter/plugin')(on);
            return config;
        },
    },
    reporter: 'cypress-mochawesome-reporter',
    reporterOptions: {
        reportDir: 'cypress/results',
        charts: true,
        reportPageTitle: 'Login E2E Test Report',
        embeddedScreenshots: true,
        overwrite: false,
        html: true,
        json: true,
    },
});
