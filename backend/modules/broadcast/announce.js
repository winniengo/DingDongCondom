/*
 * TSB - announce.js
 * 
 * returns whether or not the TSB service is open for business 
 * also returns an announcement string.
 *
 */


var Announcement = require('./models').Announcement; 
var User = require('../user/models.js').User;

var gcm = require('node-gcm');

exports.set_announcement = function(message, open_for_business, callback) {
	var sender = new gcm.Sender('AIzaSyChUqVv6OSHR58eElHGTYOYJj3IbXgCZ5Y');

 	// or with object values
	var gcmMessage = new gcm.Message({
	    collapseKey: 'DingDong:Condom',
	    delayWhileIdle: true,
	    timeToLive: 3,
	    data: {
	    	type: 'broadcast',
	        message : message,	    
		}
		
	});

	var open_for_business_bool;
	if (open_for_business) {
		open_for_business_bool = true;
	} else {
		open_for_business_bool = false;
	}

	console.log('msg in announce: ' + message);

	// set the annoucement 
	Announcement.findOneAndUpdate({announcement_id: 'ANDROID_ANNOUNCEMENT'}, 
		{message: message, open_for_delivery: open_for_business_bool}, function(err, announcement){
			if (err) {
				console.log('Error in set_announcement: ' + err);
				callback({'response' : 'ANNOUNCEMENT_SETANNOUNCEMENT_ERROR'}, 500);
			} 
			if (announcement) {
				console.log('Set new announcement: ' + announcement);
				fetchAllUserPushIDs(function(push_ids) {
					sender.send(gcmMessage, push_ids , 4, function(err, result) {
									if (err) {
										console.log('Broadcast sender err: ' + err);
									} else {
										console.log("Broadcast send: All done.");
									}
								});			

				});
			
				callback({'response' : 'ANNOUNCEMENT_SETANNOUNCEMENT_SUCCESS'}, 200);
			}
		});
}

function fetchAllUserPushIDs (callback) {
	// send a GCM notification to everyone 

	User.find({}, function (err, users){
		var push_ids = [];

		if (err) {
			console.log('in set_announcement: ' + err);
		} 
		for (i=0; i<users.length; i++) {
			var id = users[i].push_id;
			if (id) {
				push_ids.push(users[i].push_id);
			}
		}

		callback(push_ids);
	});
}



exports.get_announcement  = function(callback) {

	// get the annoucements 
	Announcement.findOne({announcement_id: 'ANDROID_ANNOUNCEMENT'}, function(err, announcement) {
		if (err) {
			console.log('Error retrieving announcements');
		}
		if (announcement) {
			callback({'response:' : 'ANNOUNCEMENT_GETANNOUNCEMENT_SUCCESS',
					  'message' : announcement.message,
					  'open_for_business' : announcement.open_for_business}, 200);
		} else {
			callback({'response': 'ANNOUNCEMENT_GETANNOUNCEMENT_ERROR'}, 500);
		}

	});

}

exports.initialize_annoucement = function() {

	Announcement.findOne({announcement_id: 'ANDROID_ANNOUNCEMENT'}, function(err, announcement) {

		if (err) {
			console.log('Error in initialize_annoucnement: ' + err);
		}
		if (announcement) {
			console.log('Announcement for Android exists, is: ' + announcement.message);
		} else {
			var new_announcement = new Announcement({
				announcement_id : 'ANDROID_ANNOUNCEMENT',
				message : 'Thank for your installing DingDong: Condom! We will be announcing changes and information here.',
				open_for_business : false
			});

			new_announcement.save(function(err){
				if (err) {
					console.log('Error saving a new announcement: ' + err);
				} else {
					console.log('New Android annoucement saved: ' + new_announcement);
				}
			});
		}



	});


}