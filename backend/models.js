/*
 * TSB - Models.JS file
 * Defines the TSB Backend's database models
 */

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = mongoose.Schema({
    token : String,
    uuid: String,
    hashed_auth_token: String,
    register_date : Date
    //add more fields for various stuff

});

var condomLifecycleSchema = mongoose.Schema({
    order_number : String, 
    
    requester : String, //uuid of the requesting device

    date_requested : Date,
    date_accepted: Date,
    date_delivered: Date,
    
    delivery_destination : { 
	dorm_name : String,
	dorm_room : Number, 
	delivery_type : String, 
	geo : {lat: Number, lon: Number} //field for geo location
	},
    
    comments : [ {body: String, date: Date} ]

});


mongoose.connect('mongodb://localhost:27017/tsb-db');


// export the different schemas as models
module.exports = mongoose.model('users', userSchema);
module.exports = mongoose.model('condomLifecyle', condomLifecycleSchema);
