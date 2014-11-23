/*
 * TSB - Survey/Retrieve.JS file
 * API call to retrieve a survey as JSON given a campaign ID. 
 */


var mongoose = require('mongoose');
var surveyPrototype = require('./models').surveyProtype;
var survey = require('./models').survey;
var campaign = require('./models').surveyCampaign;
// import crontab here too

exports.retrieve = function (session_token, campaign_id, callback) {

	// retrieve the campaign for the given campaign_id

	campaign.find ({campaign_id:campaign_id}, function(err, campaigns) {
		if (users.length == 0) {
			callback({'response' : 'SURVEY_RETRIEVE_ERROR_CAMPAIGN_NOT_FOUND'});
		} else {
			var campaign = campaigns[0];
		}
	}

	// find the survey text

	surveyPrototype.find ({survey_id : campaign.protoype_survey_id}, function(err, surveys) {
		if (surveys.length == 0) {
			callback({'response' : 'SURVEY_RETRIEVE_ERROR_SURVEY_NOT_FOUND'});
		} else {
			var survey_prototype = surveys[0];
		}
	}

	// return the survey in the response as JSON + campaign ID so they know what they're posting back to

	callback({'response' : 'SURVEY_RETRIEVE_SUCCESS',
			 'campaign_id' : campaign_id, 
			 'survey_body' : survey_prototype.survey_body,
			}, 200);

}


exports.complete = function (session_token, campaign_id, survey, callback) {

	// find the user ref
    user.find ({session_token : session_token}, function(err, users) {
		if (users.length == 0) {
			callback({'response': 'SURVEY_RETRIEVE_ERROR_USER_NOT_FOUND'}, 400);
		} else {
			var user = users[0];
			var device_uuid = users[0].device_uuid;
		}
	});

	// find the campaign document
	campaign.find ({campaign_id:campaign_id}, function(err, campaigns) {
		if (users.length == 0) {
			callback({'response' : 'SURVEY_RETRIEVE_ERROR_CAMPAIGN_NOT_FOUND'});
		} else {
			var campaign = campaigns[0];
		}
	}

	// find the survey text

	surveyPrototype.find ({survey_id : campaign.protoype_survey_id}, function(err, surveys) {
		if (surveys.length == 0) {
			callback({'response' : 'SURVEY_RETRIEVE_ERROR_SURVEY_NOT_FOUND'});
		} else {
			var survey_prototype = surveys[0];
		}
	}



	// now add a survey object for this respective user
	var user_survey = new survey({

		campaign_id : campaign._id,

	    survey_id : campaign.prototype_survey_id,
	    survey_title : survey_prototype.survey_title,

	    participant_device_uuid : device_uuid, //uuid of the requesting device
	    participant_most_recent_order_number : '',

	    // specifics
	    survey_body : survey
	})

	survey.find({participant_device_uuid : device_uuid, campaign_id : campaign._id}
				, function (err, surveys) {
		if (surveys.length == 0) {
			user_survey.save()
		} else {
			callback({'response' : 'SURVEY_COMPLETE_ERROR_DUPLICATE_REPLY'});
		}
	}

	// move user to completed_users array

	var eligible_users = campaign.eligible_users;
	var pending_users = campaign.pending_users;
	var completed_users = campaign.completed_users;


	eligible_users.remove(user._id);
	pending_users.remove(user._id);
	completed_users.add(user._id);


	// return success code to the client
	callback({'response' : 'SURVEY_COMPLETE_SUCCESS'}, 201)


}
