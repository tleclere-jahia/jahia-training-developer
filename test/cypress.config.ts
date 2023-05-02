import {defineConfig} from "cypress";

export default defineConfig({
    chromeWebSecurity: false,
    defaultCommandTimeout: 10000,
    videoUploadOnPasses: false,
    viewportWidth: 1366,
    viewportHeight: 768,
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
        baseUrl: 'http://localhost:8080',
    },
});
