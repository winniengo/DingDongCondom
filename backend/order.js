/*
 * TSB - order.js
 * 
 * handles all condom ordering aspects
 *
 */



var mongoose = require('mongoose');
var crypto = require('crypto');
var order = require('./models').orders;
var user = require('./models').users;
var shortid = require('shortid');

exports.request = function (dorm_name, dorm_room, delivery_type, 
			    date_requested, callback) {
    var dn = dorm_name;
    var dr = dorm_room;
    var dt = delivery_type;
    var drt = date_requested;

    var now = new Date();
    var oid = shortid.generate();
    
    //debug
    console.log("request got in at:", new Date());



    // find the user from device_id 
    var device_uuid = "00000000";


    var new_order = new order({
	order_number : oid, 

	requester : device_uuid, 
	deliverer : "",
	
	order_received : true, 
	order_accepted : false,
	order_delivered : false,
	order_failed : false,
	
	date_requested: now, 
	date_accepted : null, 
	date_delivered: null, 

	delivery_estimate : -1,

	delivery_destination : {
	    dorm_name : dorm_name, 
	    dorm_room : dorm_room, 
	    delivery_type : delivery_type, 
	    geo : {
		lat: 0, 
		lon: 0,
	    }
	}

	
	
    });


    order.find({order_number:oid}, function (err, orders) {
	var len = orders.length;
	if (len == 0) {
	    //order doesn't exist, so let's create it
	    new_order.save(function (err) {
		callback({'response': "Order successfully placed",
			 'order_number': oid}, 200);
		});
	} else {
	    callback({'response':"Order failed, please try again"}, 400);
	}

    });

}


exports.status = function(order_number, callback) {

    var oid = order_number;

    order.find({order_number:oid}, function (err, orders) {
	var len = orders.length;
	var succes_code = 200;
	var error_code = 404;

	if (len == 0) {
	    // the order doesn't exist
	    callback({'response':'Order does not exist'}, error_code);
	} else {
	    var or = orders[0];
	    var order_accepted = or.order_accepted;
	    var order_delivered = or.order_delivered;
	    var order_failed = or.order_failed;
	    
	    var date_accepted = or.date_accepted;
	    var delivery_estimate = or.delivery_estimate;

	    callback({
		'response' : 'Order exists',
		'order_accepted' : order_accepted,
		'order_delivered' : order_delivered,
		'order_failed' : order_failed,
		
		'date_accepted' : date_accepted,
		'delivery_estimate' : delivery_estimate,
		
		}, success_code);
	}


    });

}

	    
	    
