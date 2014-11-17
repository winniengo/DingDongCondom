/*
 * TSB - Middleware.JS file 
 * Useful middleware for the TSB project
 */


var mongoose = require('mongoose');
var user = require('../user/models').users;
var order = require('../delivery/models').orders;



exports.get_device_uuid = function (req, res) {
	var token = req.body.session_token

	user.find ({session_token : token}, function(err, users) {
		if (users.length == 0) {
			res.status(401).end("ERROR_NOT_AUTHENTICATED");
		} else {
			return users[0].device_uuid;
		}
	});

}

//check if a user is authenticated, otherwise redirect them to a 403 page

exports.is_authenticated = function (req, res, next) {

	// check if the user is authenticated (i.e. provides a session token)
	var token = req.body.session_token;

	// check if that token is valid
	user.find ({session_token : token}, function(err, users) {
		if (users.length == 0) {
			res.status(401).end("ERROR_NOT_AUTHENTICATED");
		} else {
			return next();
		}
	});

}

exports.is_authenticated_and_requester = function (req, res, next) {

	// check if the user is authenticated (i.e. provides a session token)
	var token = req.body.session_token;
	var order_number = req.body.order_number;

	// check if that token is valid
	user.find ({session_token : token}, function(err, users) {
		if (users.length == 0) {
			res.status(401).end("ERROR_NOT_AUTHENTICATED");
		} else {
			var device_uuid = users[0].device_uuid;
			order.find( {order_number : order_number} , function(err, orders) {
				if (orders.length == 0) {
					res.status(404).end("ERROR_ORDER_NOT_FOUND");
				} else {
					console.log('rq:' + orders[0].requester + '  duuid: ' + device_uuid);
					if (orders[0].requester == device_uuid) {
						return next();
					} else {
						res.status(403).end('ERROR_FORBIDDEN');
					}
				}

			});
		}
			
	});

}