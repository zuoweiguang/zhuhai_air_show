// require all `test/**/*-test.js`
var testContext = require.context('./test', true, /-test\.js?$/);
testContext.keys().forEach(testContext);
