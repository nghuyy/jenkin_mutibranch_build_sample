const path = require('path')
const webpack = require('webpack')
const nodeExternals = require('webpack-node-externals')
const CopyPlugin = require("copy-webpack-plugin")

module.exports = {
    mode: "production",
    entry:'./app.js',
    target: "node",
    output: {
        filename: 'app.js',
        path: path.resolve(__dirname, './dist')
    },
    resolve: {
        modules: [
            "node_modules",
            "./routers",
            "./midlewares",
            "./const",
            "./lang",
        ]
    },
    externals: [nodeExternals()],
    /*
    plugins: [
            new CopyPlugin({
                patterns: [
                    { from: "./views", to: "./views" }
                ],
                options: {
                    concurrency: 100,
                },
            })
        ]
     */
}