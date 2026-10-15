import {defineConfig} from "cypress";

// Since Cypress 10 the plugins file is ignored, so the environment that @jahia/cypress reads
// (JAHIA_URL, JAHIA_PROCESSING_URL, SUPER_USER_PASSWORD) must be set here.
// Override with the JAHIA_URL, JAHIA_PROCESSING_URL and SUPER_USER_PASSWORD environment variables.
export default defineConfig({
    chromeWebSecurity: false,
    defaultCommandTimeout: 10000,
    viewportWidth: 1366,
    viewportHeight: 768,
    e2e: {
        setupNodeEvents(on, config) {
            const jahiaUrl = process.env.JAHIA_URL || config.env.JAHIA_URL || 'http://localhost:8080';
            config.baseUrl = jahiaUrl;
            config.env.JAHIA_URL = jahiaUrl;
            config.env.JAHIA_PROCESSING_URL = process.env.JAHIA_PROCESSING_URL || config.env.JAHIA_PROCESSING_URL || jahiaUrl;
            config.env.SUPER_USER_PASSWORD = process.env.SUPER_USER_PASSWORD || config.env.SUPER_USER_PASSWORD;
            on('task', {
                log(message: string) {
                    console.log(message);
                    return null;
                }
            });
            return config;
        },
        baseUrl: 'http://localhost:8080',
    },
    env: {
        SUPER_USER_PASSWORD: 'root',
    },
});
