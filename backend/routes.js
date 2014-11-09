/*
 * TSB - Routes.JS file 
 * Defines the overall form of our API
 */

// worker functions 
var register = require('config/register');
var login = require('config/login');

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
	login.login(uuid, auth_token,function (found) {
	    console.log(found);
	    res.json(found);
	});
    });
    
    // register with the backend by supplying 
    //     - a (persistent) device uuid
    //     - a signup_code that is distributed to each user

    app.post('/api/register',function(req,res){
	var uuid = req.body.uuid;
        var signup_code = req.body.signup_code;
	register.register(uuid, signup_code, function (found) {
	    console.log(found);
	    res.json(found);
	});
    });


};
