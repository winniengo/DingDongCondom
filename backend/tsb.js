/*
 *  TSB - Node.JS Backend for the Android and iOS app
 *  Version 0.0.1 - November 9, 2014
 */

var express  = require('express');
var connect  = require('connect');
var app      = express();
var port     = process.env.PORT || 8080;

// Configuration
app.use(express.static(__dirname + '/public'));
app.use(connect.logger('dev'));
app.use(connect.json());
app.use(connect.urlencoded());

// Routes
require('./routes/routes.js')(app);
app.listen(port);


console.log('TSB is running on port ' + port);
