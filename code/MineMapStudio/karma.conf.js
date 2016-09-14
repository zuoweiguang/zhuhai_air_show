var path = require('path');

var PATHS = {
    rootPath: path.resolve(__dirname),
    srcPath: path.resolve(__dirname, 'src'),
    testPath: path.resolve(__dirname, 'test')
};

module.exports = function (config) {
    config.set({
        browsers: ['Chrome'],
        singleRun: true,
        frameworks: ['jasmine'],
        files: [
            'test.webpack.js'
        ],
        preprocessors: {
            'test.webpack.js': ['webpack', 'sourcemap']
        },
        webpack: {
            devtool: 'inline-source-map',
            module: {
                loaders: [
                    {
                        test: /\.js?$/,
                        loader: 'babel',
                        include: [PATHS.srcPath, PATHS.testPath]
                    },
                    {
                        test: /\.scss$/,
                        loaders: ['style', 'css', 'sass']
                    },
                    {
                        test: /\.(png|jpg)$/,
                        loader: 'url?limit=25000'
                    }
                ]
            },
            resolve: {
                extensions: ['', '.js', '.jsx']
            }
        }
    });
};

