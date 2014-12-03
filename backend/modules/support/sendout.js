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
 				
 			for (i=0;i<eligible_users.length;i++) {
	 			User.findOne({ _id : eligible_users[i] }, function(err, user){
	 					if (err) {
	 						console.log('(Sendout.js): Error: ' + err);
	 					}
	 					if (user) {
	 						if (user.device_os == 'ANDROID_OS') {
								sender.send(message, [user.push_id] , 4, function(err, result) {
									if (err) {
										console.log('Sender err: ' + err);
									}
									callback(err, "All done");
								});			
	 						} else if (user.device_os == 'IOS') {
	 							ios_push_ids.push(user.push_id);
	 						}
	 					} else {
	 						console.log('(Sendout.js): Error: User not found (ID ' + eligible_users[i] + ')');
	 					}
	 				});
	 		}

 		} else {
 			console.log('(Sendout.js): Error: Campaign ID' + campaign_id + ' not found.');
 		}

 	});

 }


exports.do_test_sendout =  function (callback) {

	Campaign.findOne({campaign_id:"TestCampaign1"}, function(err, campaign){
		var eligible_users = campaign.eligible_users;

		module.exports.survey_sendout(eligible_users, "TestCampaign1", function(err, result) {
			console.log('eligible users: ' + eligible_users);
			console.log('sender result: '+ result);
			if (err) {
				console.log('err: ' + err);
			}
		})
	});

}
