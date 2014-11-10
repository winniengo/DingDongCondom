/* 
 * TSB - Login.js
 */


var crypto = require('crypto');
var mongoose = require('mongoose');
var user = require('./models');


exports.login = function(uuid, secret, callback) {
	
    user.find( {uuid: uuid}, function (err,users) {
	
	if(users.length != 0){
	    var hp = users[0].hashed_passphrase;
	    var t = users[0].session_token;
	    var s = users[0].salt;
	    var iter = 1000;

	    // generate hash from secret input
	    try {
		var hs = crypto.pbkdf2(secret, s, iter, 64);
	    } catch {
		//error lol
	    }

	    if(hp == hs){
		callback({'response':"Login Sucess",'res':true,'token':t});
	    } else {
		callback({'response':"Invalid Passphrase",'res':false});
	    }
	} else {
	    callback({'response':"User not exist",'res':false});
	}
	
    });
}
