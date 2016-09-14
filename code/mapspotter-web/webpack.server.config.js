var path = require('path');
const webpack = require('webpack');
const CleanPlugin = require('clean-webpack-plugin');

process.env.BABEL_ENV = process.env.npm_lifecycle_event;

var PATHS = {
    rootPath: path.resolve(__dirname),
    appPath: path.resolve(__dirname, 'app'),
    srcPath: path.resolve(__dirname, 'src'),
    buildPath: path.resolve(__dirname, 'build'),
    coveragePath: path.resolve(__dirname, 'coverage'),
    assetsPath: path.resolve(__dirname, 'assets'),
    nodeModulesPath: path.resolve(__dirname, 'node_modules'),
    indexEntry: path.resolve(__dirname, 'src/index.js')
};

module.exports = {
    devtool: 'cheap-module-eval-source-map',
    entry: {
        index: [
            'webpack-hot-middleware/client',
            'webpack/hot/only-dev-server',
            PATHS.indexEntry
        ]
    },
    output: {
        path: PATHS.buildPath,
        filename: '[name].bundle.js',
        publicPath: '/assets/'
    }
    ,
    resolve: {
        extensions: ['', '.js', '.jsx']
    }
    ,
    module: {
        loaders: [
            {
                test: /\.js|jsx?$/,
                loaders: ['react-hot', 'babel'],
                include: PATHS.srcPath
            },
            {
                test: /\.scss$/,
                loaders: ['style', 'css', 'sass']
            },
            {
                test: /\.css/,
                loaders: ['style', 'css']
            },
            {
                test: /\.(png|jpg)$/,
                loader: 'url?limit=50000'
            },
            {
                test: /\.json$/,
                loaders: ['json'],
                exclude: /node_modules/,
                include: __dirname
            },
            {
                test: /\.js$/,
                include: path.resolve(__dirname, 'node_modules/webworkify-webpack/index.js'),
                loader: 'worker'
            }
        ]
    }
    ,
    plugins: [
        new webpack.optimize.OccurrenceOrderPlugin(),
        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoErrorsPlugin()
    ]
}
