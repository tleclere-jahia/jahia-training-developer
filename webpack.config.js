const path = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const getModuleFederationConfig = require('@jahia/webpack-config/getModuleFederationConfig');
const packageJson = require('./package.json');

const MODULE_NAME = 'jahia-training-developer';

module.exports = (env, argv) => {
    let config = {
        entry: {
            main: [path.resolve(__dirname, 'src/javascript/index.js')]
        },
        output: {
            path: path.resolve(__dirname, 'src/main/resources/javascript/apps/'),
            filename: `${MODULE_NAME}.bundle.js`,
            chunkFilename: `[name].${MODULE_NAME}.[chunkhash:6].js`
        },
        resolve: {
            mainFields: ['module', 'main'],
            extensions: ['.js', '.jsx']
        },
        module: {
            rules: [
                {
                    test: /\.m?js$/,
                    type: 'javascript/auto'
                },
                {
                    test: /\.jsx?$/,
                    include: [path.join(__dirname, 'src')],
                    use: {
                        loader: 'babel-loader',
                        options: {
                            presets: [
                                ['@babel/preset-env', {modules: false, targets: {safari: '7', ie: '10'}}],
                                '@babel/preset-react'
                            ],
                            plugins: [
                                '@babel/plugin-syntax-dynamic-import'
                            ]
                        }
                    }
                }
            ]
        },
        plugins: [
            new ModuleFederationPlugin(getModuleFederationConfig(packageJson, {
                remotes: {
                    '@jahia/jcontent': 'appShell.remotes.jcontent'
                }
            })),
            new CleanWebpackPlugin({
                cleanOnceBeforeBuildPatterns: [`${path.resolve(__dirname, 'src/main/resources/javascript/apps/')}/**/*`],
                verbose: false
            }),
            new CopyWebpackPlugin({patterns: [{from: './package.json', to: ''}]})
        ],
        mode: 'development'
    };

    config.devtool = (argv.mode === 'production') ? 'source-map' : 'eval-source-map';

    if (argv.analyze) {
        config.devtool = 'source-map';
        config.plugins.push(new BundleAnalyzerPlugin());
    }

    return config;
};
