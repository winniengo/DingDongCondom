/*
 *  TSB - Node.JS Backend for the Android and iOS app
 *  Version 0.0.1 - November 9, 2014
 */

var express   = require('express');
var connect   = require('connect');
var app       = express();

var port      = process.env.PORT || 8080;
var httpsPort = 8443;
var db 		  = require('./modules/db/db.js');
var fs 		  = require('fs');

// SSL configuration
//var privateKey = fs.readFileSync('etc/ssl/private/server.key', 'utf8');
//var certificate = fs.readFileSync('etc/ssl/private/server.crt', 'utf8');
var http = require('http');
var https = require('https');
//var credentials = {key: privateKey, cert: certificate};

// Configuration
app.use(express.static(__dirname + '/public'));
app.use(connect.logger('dev'));
app.use(connect.json());
app.use(connect.urlencoded());

// Routes
require('./routes.js')(app);
//app.listen(port);


// Set up the servers
var httpServer = http.createServer(app);
//var httpsServer = https.createServer(credentials, app);

// Listen on the respective ports
httpServer.listen(port);
//httpsServer.listen(httpsPort);


console.log('TSB is running on port ' + port);
