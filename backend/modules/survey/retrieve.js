/*
 * TSB - Survey/Retrieve.JS file
 * API call to retrieve a survey as JSON given a campaign ID. 
 */


var mongoose = require('mongoose');
var SurveyPrototype = require('./models').SurveyPrototype;
var Survey = require('./models').Survey;
var Campaign = require('./models').SurveyCampaign;
var User = require('../user/models').User;

// import crontab here too

exports.retrieve = function (session_token, campaign_id, callback) {

	var campaign;
	var user;
	var survey_prototype;

	var user_query = User.where({session_token : session_token});
	user_query.findOne(function(err, found){
		if (err) {
			callback({'response' : 'SURVEY_RETRIEVE_DATABASE_ERROR'}, 500);
		}
		if (found) {
			user = found;

			var campaign_query = Campaign.where({campaign_id:campaign_id});
			campaign_query.findOne(function(err, found) {
				if (err) {
					callback({'response' : 'SURVEY_RETRIEVE_DATABASE_ERROR'}, 500);
				}
				if (found) {
					campaign = found;

					var survey_query = SurveyPrototype.where({ _id : campaign.prototype_survey_id });

					console.log('id: ' + campaign.prototype_survey_id );
					survey_query.findOne(function(err, found) {
						if (err) {
							callback({'response' : 'SURVEY_RETRIEVE_DATABASE_ERROR'}, 500);
						}
						console.log('found :' + found);
						if (found) {
							var survey_prototype = found;
							var survey_prototype_body = [];

							for (i=0; i<survey_prototype.survey_body.length;i++) {
								survey_prototype_body.push({
									'question_id' : survey_prototype.survey_body[i].question_id,
									'question_title' : survey_prototype.survey_body[i].question_title,
									'question' : survey_prototype.survey_body[i].question,
									'response' : survey_prototype.survey_body[i].response,
								});
							}

							// add the user to the pending list
							// find by document id and update

							campaign.eligible_users.pull(user._id);
							campaign.pending_users.push(user._id);
							campaign.save(function(err){
								if(err) {
									console.log('in retrieve (54): '+ err);
								}
							});

							// return the survey in the response as JSON + campaign ID 
							// so they know what they're posting back to
							callback({'response' : 'SURVEY_RETRIEVE_SUCCESS',
								 'campaign_id' : campaign.campaign_id, 
								 'survey_body' : survey_prototype_body,
								}, 200);

						} else {
							callback({'response' : 'SURVEY_RETRIEVE_ERROR_SURVEY_NOT_FOUND'}, 404);
						}
					});
				}
				else {
					callback({'response' : 'SURVEY_RETRIEVE_ERROR_CAMPAIGN_NOT_FOUND'}, 404);
				}
			});
		} else {
			callback({'response' : 'SURVEY_RETRIEVE_USER_NOT_FOUND'}, 403);
		}
	});

}


exports.complete = function (session_token, campaign_id, answers, callback) {

	// find the user ref
    User.find ({session_token : session_token}, function(err, users) {
		if (users.length == 0) {
			callback({'response': 'SURVEY_COMPLETE_ERROR_USER_NOT_FOUND'}, 400);
		} else {
			var user = users[0];
			var device_uuid = users[0].device_uuid;

			// find the campaign document
			Campaign.find ({campaign_id:campaign_id}, function(err, campaigns) {
				if (users.length == 0) {
					callback({'response' : 'SURVEY_COMPLETE_ERROR_CAMPAIGN_NOT_FOUND'}, 404);
				} else {
					var campaign = campaigns[0];

					// find the survey text

					var survey_query = SurveyPrototype.where({ _id : campaign.prototype_survey_id });

					survey_query.findOne(function(err, survey) {
						if (err) {
							callback({'response' : 'SURVEY_COMPLETE_DATABASE_ERROR'}, 500);
						} 
						if (survey) {
							var survey_prototype = survey;

							// now add a survey object for this respective user
							var user_survey = new Survey({

								campaign_id : campaign._id,

							    survey_id : campaign.prototype_survey_id,
							    survey_title : survey_prototype.survey_title,

							    participant_device_uuid : device_uuid, //uuid of the requesting device
							    participant_most_recent_order_number : '',

							    survey_body : JSON.parse(answers)
							});

							user_survey.save(function(err){
								if (err) {
									console.log('in retrieve.js(123): ' + err);
									return callback({'response' : 'SURVEY_COMPLETE_DATABASE_ERROR'}, 500);
								} else {
									// move user to completed_users array

									var eligible_users = campaign.eligible_users;
									var pending_users = campaign.pending_users;
									var completed_users = campaign.completed_users;

									eligible_users.remove(user._id);
									pending_users.remove(user._id);
									completed_users.push(user._id);

									Campaign.save(function(err) {
										if (err) {
											console.log('in retrieve.js(139): ' + err);
											return callback({'response' : 'SURVEY_COMPLETE_DATABASE_ERROR'}, 500);
										} else {
											// return success code to the client
											callback({'response' : 'SURVEY_COMPLETE_SUCCESS'}, 201);
										}
									});

								} 

							});

							
						} else {
							callback({'response' : 'SURVEY_COMPLETE_ERROR_SURVEY_NOT_FOUND'}, 404);
						}

						
						
					});


				}
			});

		}
	});

}


exports.create_test_campaign = function (callback) {

	
	var new_survey_prototype = new SurveyPrototype({
		survey_id : '123456789',
	    survey_title : 'Survey1',

	    // the actual questions
	    survey_body : [ {question_id : '1', question_title: 'Your Yoghurt', 
	    				 question: 'Do you like yoghurt?',
	    				 response: ''},
	    				 {question_id : '2', question_title: 'Your Yoghurt 2', 
	    				 question: 'Are you sure?',
	    				 response: ''},
	    				 {question_id : '3', question_title: 'Your Yoghurt 3', 
	    				 question: 'Like really?',
	    				 response: ''},
	    			  ],

	});
	

	console.log('test');


	new_survey_prototype.save();


	var u_id;
	var q = User.where({device_uuid:'328fb58d8a279160'});
	q.findOne(function(err, user){
		if (err) {
			console.log("couldn't get user 123456");
		} 
		if (user) {
			u_id = user._id;
			console.log('u_id: ' + u_id);

			var new_campaign = new Campaign({

				campaign_id : "TestCampaign1",
		        campaign_title : "I am testing the campaign feature", 

		        prototype_survey_id : new_survey_prototype._id, 
		        completed_survey_ids : [],

		        eligible_users : [u_id],
		        pending_users : [],
		        completed_users : [],

		        crontab : "* * * * * *", //the crontab on which this campaign gets executed

		    });
		    
		    // TODO : Somehow incorporate a campaign end date
	        new_campaign.save();

	        callback({'response' : 'all done'}, 200);

		} else {
			console.log('user not found');
		}
	});


	


}


