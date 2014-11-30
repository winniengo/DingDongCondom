/* 
 * TSB - Login.js
 */


var crypto = require('crypto');
var mongoose = require('mongoose');
var user = require('./models').users;


exports.login = function(device_uuid, secret, callback) {
	
    user.find( {device_uuid: device_uuid}, function (err,users) {
	
	if(users.length != 0){
	    var hp = users[0].hashed_passphrase;
	    var t = users[0].session_token;
	    var session_token_expires = users[0].session_token_expires;
	    //var s = users[0].salt;
	    var s = '123';

	    var iter = 1000;	   

	    // generate hash from secret input
	    try {
		var hs = crypto.pbkdf2Sync(secret, s, iter, 128).toString('hex');
		console.log('hashed secret in login: ', hs);
		console.log('stored hashed secret: ', hp);
	    } catch (ex) {
		//error lol
		console.log("error hashing in /login");
	    }

	    if(hp == hs){
	    	// TODO: generate new session token and reassign it appropriately:
		
		callback({'response' : "LOGIN_SUCCESS",
			  'session_token' : t,
			  'session_token_expires' : session_token_expires,
			 }, 201);
	    } else {
			callback({'response' : "LOGIN_ERROR_INVALID_PASSPHRASE", },
				400);
	    }
	} else {
		    callback({'response':"LOGIN_ERROR_INVALID_DEVICE_UUID", },
			    400);
	}
	
    });
}


