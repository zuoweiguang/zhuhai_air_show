var path = require('path')
var webpack = require('webpack')
var HtmlWebpackPlugin = require('html-webpack-plugin');

process.env.BABEL_ENV = process.env.npm_lifecycle_event;

var PATHS = {
    rootPath: path.resolve(__dirname),
    appPath: path.resolve(__dirname, 'app'),
    srcPath: path.resolve(__dirname, 'src'),
    buildPath: path.resolve(__dirname, 'build'),
    coveragePath: path.resolve(__dirname, 'coverage'),
    nodeModulesPath: path.resolve(__dirname, 'node_modules'),
    indexEntry: path.resolve(__dirname, 'src/index.js')
};

module.exports = {
    entry: {
        index: PATHS.indexEntry
    },
    output: {
        path: PATHS.buildPath,
        filename: '[name].bundle.js',
        publicPath: '/assets/'
    },
    devServer: {
        historyApiFallback: true,
        hot: true,
        inline: true,
        progress: true
    },
    resolve: {
        extensions: ['', '.js', '.jsx']
    },
    node: {
        debug: true,
        fs: 'empty'
    },
    module: {
        loaders: [
            {
                test: /\.js|jsx?$/,
                loaders: ['babel'],
                include: PATHS.srcPath
            },
            {
                test: /\.scss$/,
                loaders: ['style', 'css', 'sass']
            },
            {
                test: /\.css$/,
                loaders: ['style', 'css']
            },
            {
                test: /\.json$/,
                loader: 'json-loader'
            }, ,
            {
                test: /\.(png|jpg)$/,
                loader: 'url?limit=50000'
            }
        ]
    },
    plugins: [
        new webpack.optimize.OccurrenceOrderPlugin(),
        new webpack.NoErrorsPlugin(),
        new webpack.optimize.UglifyJsPlugin({
            minimize:true
        }),
        new webpack.DefinePlugin({
            "process.env": {
                NODE_ENV: JSON.stringify("production")
            }
        })
    ]
}
