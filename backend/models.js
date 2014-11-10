/*
 * TSB - Models.JS file
 * Defines the TSB Backend's database models
 */

var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = mongoose.Schema({
    uuid : String,
    hashed_passphrase: String,
    session_token : String,
    salt : String,

    register_date : Date,

    //add more fields for various stuff

});

var orderLifecycleSchema = mongoose.Schema({
    order_number : String, 
    
    requester : String, //uuid of the requesting device
    deliverer : String, //uuid of the deliverer

    // status fields
    order_received : Boolean,
    order_accepted : Boolean,
    order_delivered : Boolean,
    order_failed : Boolean,

    // timestamp fields
    date_requested : Date,
    date_accepted: Date,
    date_delivered: Date,
    

    // specifics
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
module.exports = mongoose.model('orderLifecyle', orderLifecycleSchema);
