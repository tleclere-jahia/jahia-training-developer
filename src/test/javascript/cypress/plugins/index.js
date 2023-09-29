module.exports = (on, config) => {
    require('@jahia/cypress/dist/plugins/registerPlugins').registerPlugins(on, config);
    return config;
};
