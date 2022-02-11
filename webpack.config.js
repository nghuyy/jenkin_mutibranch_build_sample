const path = require('path')
const webpack = require('webpack')
const CopyPlugin = require("copy-webpack-plugin")
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");
require("style-loader")

module.exports = {
    mode: "production",
    entry:'./src/client.js',
    output: {
        filename: 'script.js',
        path: path.resolve(__dirname, './dist/public')
    },
    resolve: {
        modules: [
            "node_modules"
        ]
    },
    plugins: [
        new MiniCssExtractPlugin(),
        new CopyPlugin({
            patterns: [
                {from: "./LICENSE", to: "./LICENSE" },
                {from: "./CHANGELOG.txt", to: "./CHANGELOG.txt" }
            ],
            options: {
                concurrency: 100,
            },
        })
    ],
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: [MiniCssExtractPlugin.loader, "css-loader"],
            },
            {
                test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name: '[name].[ext]',
                            outputPath: 'fonts/'
                        }
                    }
                ]
            }
        ]
    },
    optimization: {
        minimizer: [
            new CssMinimizerPlugin(),
            new TerserPlugin()
        ],
        minimize: true
    }
}
