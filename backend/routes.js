/*
 * TSB - Routes.JS file 
 * Defines the overall form of our API
 */

// worker functions 
var register = require('./register');
var login = require('./login');

module.exports = function(app) {
    app.get('/', function(req, res) {
	res.end("TSB Backend v0.0.1");
    });

    // authenticate with the backend by supplying
    //   - a persistent device uuid
    //   - an auth_token that was given by the backend

    
    app.post('/api/login',function(req,res){
	var uuid = req.body.uuid;
        var auth_token = req.body.auth_token;
	login.login(uuid, auth_token, function (found) {
	    console.log(found);
	    res.json(found);
	});
    });
    
    // register with the backend by supplying 
    //     - a (persistent) device uuid
    //     - a signup_code that is distributed to each user

    app.post('/api/register',function(req,res){
	var uuid = req.body.device_uuid;
        var passphrase = req.body.passphrase;
	var signup_token = req.body.signup_token;
	
	console.log("in routes.js, uuid:", uuid);
	
	register.register(uuid, passphrase, signup_token, function (found) {
	    console.log(found);
	    res.json(found);
	});
    });


};
