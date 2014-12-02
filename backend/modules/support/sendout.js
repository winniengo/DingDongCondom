/*
 * TSB - sendout.JS file 
 * Functions to send out push notifications via GCM (and APN)
 */




 var gcm = require('node-gcm');
 var Promise = require('bluebird');

 // models we need
 var Campaign = require('../survey/models.js').SurveyCampaign;
 var User = require('../user/models.js').User;


 exports.survey_sendout = function(push_id_list, campaign_id, callback) {

 	var sender = new gcm.Sender('AIzaSyChUqVv6OSHR58eElHGTYOYJj3IbXgCZ5Y');

 	// or with object values
	var message = new gcm.Message({
	    collapseKey: 'DingDong:Condom',
	    delayWhileIdle: true,
	    timeToLive: 3,
	    data: {
	        campaign_id : campaign_id,
	    }
	});

 	Campaign.findOne({ campaign_id : campaign_id }, function(err, campaign){
 		if (err) {
 			console.log('(Sendout.js): Error: ' + err);
 		}
 		if (campaign) {
 			var eligible_users = campaign.eligible_users;
 			var android_push_ids = [];
 			var ios_push_ids = [];

 			Promise.map(eligible_users, function(user_id) {
 				User.findOne({ _id : user_id }, function(err, user){
 					if (err) {
 						console.log('(Sendout.js): Error: ' + err);
 					}
 					if (user) {
 						if (user.device_os == 'ANDROID_OS') {
 							return android_push_ids.push(user.push_id);
 						} else if (user.device_os == 'IOS') {
 							return ios_push_ids.push(user.push_id);
 						}
 					} else {
 						console.log('(Sendout.js): Error: User not found (ID ' + eligible_users[i] + ')');
 					}
 				});
 			}).done(function (){
 				sender.send(message, android_push_ids, 4, function(err, result) {
 					if (err) {
 						console.log(err);
 					}
 					console.log(result);
 				});
 			});

 		} else {
 			console.log('(Sendout.js): Error: Campaign ID' + campaign_id + ' not found.');
 		}

 	});

 }
