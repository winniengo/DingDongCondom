/*
 *  TSB - Node.JS Backend for the Android and iOS app
 *  Version 0.0.1 - November 9, 2014
 */

var express  = require('express');
var connect  = require('connect');
var app      = express();
var port     = process.env.PORT || 8080;
var db 		 = require('./modules/db/db.js');

// Configuration
app.use(express.static(__dirname + '/public'));
app.use(connect.logger('dev'));
app.use(connect.json());
app.use(connect.urlencoded());

// Routes
require('./routes.js')(app);
app.listen(port);


console.log('TSB is running on port ' + port);
