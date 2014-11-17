/* 
 * TSB - Register.js
 */

var mongoose = require('mongoose');
var crypto = require('crypto');
var user = require('./models').users;


exports.register = function(device_uuid, passphrase, signup_token, device_os, callback) {
    var u = device_uuid;
    var p = passphrase;
    var st = signup_token;
    var dos = device_os;

    // check that the signup_token is correct
    if (st != "TSBSignupToken2014") {
	console.log("TSBToken incorrect, token was:", signup_token);
    } 
	
    // do crypto to hash passphrase and generate auth_token
    
    var iter = 1000 //num iterations of Hash function

    try {
	var s = crypto.randomBytes(10).toString('hex');
    } catch (ex) {
	// problem generating the salt, should handle this
	console.log("problem generating salt in register()");
    }
    
    try {
	var hp = crypto.pbkdf2Sync(p, s, iter, 128).toString('hex'); 
    } catch (ex) {
	console.log("problem generating hash in register()");
	// problem generating hash, probably should handle this
    }
    
    // generate an initial session token and set expiry date
    var t = crypto.randomBytes(128).toString('hex');
    
    var now = new Date();
    var t_expiration = new Date(now);
    t_expiration.setHours(now.getHours() + 6);
    

    var d = new Date();
    var new_user = new user({
	device_uuid : u, 
	device_os : dos,
	
	hashed_passphrase : hp, 
	salt : s,
	
	session_token : t,
	session_token_expires : t_expiration,
	
	register_date : d,
    });

    
    //check if user exists
    user.find({uuid:u}, function (err, users) {
	var len = users.length;
	if(len == 0) {
	    //user doesn't exist yet
	    new_user.save(function (err) {
		callback({'response':"REGISTER_SUCCESS",
				  'session_token': t,
				  'session_token_expires' : t_expiration, 
				}, 
				200);
	    });
	} else {
	    callback({'response':'REGISTER_ERROR_DEVICE_ALREADY_REGISTERED'},
	    		412);
	}
    });

}
