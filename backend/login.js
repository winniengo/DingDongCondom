/* 
 * TSB - Login.js
 */


var crypto = require('crypto');
var mongoose = require('mongoose');
var user = require('./models');


exports.login = function(uuid, signup_code, callback) {
	
	user.find( {uuid: uuid}, function (err,users) {
	
	if(users.length != 0){
		var hash_db = users[0].hashed_auth_token;
		var id = users[0].token;
		//var hashed_auth_token = crypto.createHash('sha512').update(auth_token).digest("hex");
		
		if(hash_db == signup_code){
			callback({'response':"Login Sucess",'res':true,'token':id});
		}else{
			callback({'response':"Invalid Password",'res':false});
		}
	}else {
		callback({'response':"User not exist",'res':false});
	}
	
	});
}
