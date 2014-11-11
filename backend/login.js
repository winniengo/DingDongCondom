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

	    console.log("in /login, hp:",hp, "t:", t,"s:",s, "secret:", secret);

	    // generate hash from secret input
	    try {
		var hs = crypto.pbkdf2Sync(secret, s, iter, 64).toString('hex');
	    } catch (ex) {
		//error lol
		console.log("error hashing in /login");
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
