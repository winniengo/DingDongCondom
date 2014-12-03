/*
 * TSB - announce.js
 * 
 * returns whether or not the TSB service is open for business 
 * also returns an announcement string.
 *
 */


var Announcement = require('./models').Announcement; 


exports.set_announcement = function(message, open_for_business, callback) {

	// set the annoucement 
	Announcement.findOneAndUpdate({announcement_id: 'ANDROID_ANNOUNCEMENT'}, 
		{message: message, open_for_business: open_for_business}, function(err, announcement){
			if (err) {
				console.log('Error in set_announcement: ' + err);
				callback({'response' : 'ANNOUNCEMENT_SETANNOUNCEMENT_ERROR'}, 500);
			} 
			if (announcement) {
				console.log('Set new announcement: ' + announcement);
				callback({'response' : 'ANNOUNCEMENT_SETANNOUNCEMENT_ERROR'}, 500);
			}
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