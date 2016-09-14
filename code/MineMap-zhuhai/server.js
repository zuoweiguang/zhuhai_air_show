var path = require('path')
var express = require('express')
var webpack = require('webpack')
var webpackDevMiddleware = require('webpack-dev-middleware')
var webpackHotMiddleware = require('webpack-hot-middleware')
var config = require('./webpack.server.config')

var app = express()
var port = 8080

var compiler = webpack(config)
app.use(webpackDevMiddleware(compiler, {
    noInfo: true, publicPath: config.output.publicPath, hot: true,
    historyApiFallback: true
}))
app.use(webpackHotMiddleware(compiler))

app.all('*', function (req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Content-Length, Authorization, Accept, X-Requested-With , yourHeaderFeild');
    res.header('Access-Control-Allow-Methods', 'PUT, POST, GET, DELETE, OPTIONS');
    if (req.method == 'OPTIONS') {
        res.send(200)
    } else {
        next()
    }
})

app.use('/app', express.static('app'));

app.get("/", function (req, res) {
    res.sendFile(path.join(__dirname, 'index.html'))
})

app.listen(port, function (error) {
    if (error) {
        console.error(error)
    } else {
        console.info("==> 🌎  Listening on port %s. Open up http://localhost:%s/ in your browser.", port, port)
    }
})
