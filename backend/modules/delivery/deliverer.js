/*
 * TSB - deliverer.js
 * 
 * methods for the android_deliverer app
 *
 */


var mongoose = require('mongoose');
var crypto = require('crypto');

var Order = require('./models').Order;
var User = require('../user/models').User;

var shortid = require('shortid');


exports.all = function(callback) {
	Order.find( function (err, orders){
		
		if (err) {
			callback('DELIVERY_REQUEST_ALL_ERROR_DATABASE_ERROR', 500);
		} else {
			var all = [];

			for (i in orders) {
				order_dict = orders[i];
				this_order = {
					'requester' : order_dict.requester,
					'deliverer' : order_dict.deliverer,

					'order_number' :  order_dict.order_number,
					
					'order_accepted' : order_dict.order_accepted,
					'order_delivered' : order_dict.order_delivered,
					'order_failed' : order_dict.order_failed,
					
					'date_requested' : order_dict.date_requested,
					'date_accepted' : order_dict.date_accepted,
					'date_delivered' : order_dict.date_delivered,
					
					'delivery_estimate' : order_dict.delivery_estimate,

					'delivery_destination' : order_dict.delivery_destination
				}
				all.push(this_order);
			}

			callback({'response':'DELIVERY_REQUEST_ALL_SUCCESS',
					  'orders': all}, 200);
		}

	});

}

exports.accept = function(session_token, order_number, delivery_estimate, callback) {

	//get the user's device_uuid
    User.find ({session_token : session_token}, function(err, users) {
		if (users.length == 0) {
		    callback({'response': 'DELIVERY_REQUEST_ACCEPT_ERROR_USER_NOT_FOUND'}, 400);
		} else { 
			var now = new Date();
			var deliverer = users[0].device_uuid;
			Order.findOneAndUpdate({order_number : order_number}, 
								   {order_accepted:true, 
								   	deliverer : deliverer,
								    date_accepted : now, 
								    delivery_estimate : delivery_estimate }, function(err) {
										if (err) {
											console.log(err);
										}
										callback({'response':'DELIVERY_REQUEST_ACCEPT_SUCCESS'}, 
												  200);
									});
		}

	});

}
	    
exports.deliver = function(session_token, order_number, callback) {

	//get the user's device_uuid
    User.find ({session_token : session_token}, function(err, users) {
		if (users.length == 0) {
		    callback({'response': 'DELIVERY_REQUEST_DELIVER_ERROR_USER_NOT_FOUND'}, 400);
		} else { 
			var now = new Date();
			var deliverer = users[0].device_uuid;
			Order.findOneAndUpdate({order_number : order_number}, 
								   {order_delivered: true,
								    order_accepted : true, // in case it wasn't 
								    date_delivered : now}, function(err) {
										if (err) {
											console.log(err);
										}
										callback({'response':'DELIVERY_REQUEST_DELIVER_SUCCESS'}, 
												  200);
									});
		}

	});

}