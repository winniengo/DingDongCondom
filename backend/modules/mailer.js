/*
Mails shit
*/


var nodemailer = require('nodemailer');

// create reusable transporter object using SMTP transport
var transporter = nodemailer.createTransport({
    service: 'Gmail',
    auth: {
        user: 'tsbdaemon@gmail.com',
        pass: '9854<>47f?8l05X'
    }
});

// NB! No need to recreate the transporter object. You can use
// the same transporter object for all e-mails




function tender_request(order_number, delivery_destination, requester, 
			delivery_type) {
    
    var mailOptions = {
	from: 'TSBDaemon <tsbdaemon@gmail.com>', // sender address
	to: 'tsbdaemon@gmail.com', // list of receivers
	subject: 'Order Number' + order_number + 'received', // Subject line
	text: 'X', // plaintext body
	html: '<b>X</b>' // html body
    };

    String body = "\n\n


    
    // send mail with defined transport object
    transporter.sendMail(mailOptions, function(error, info){
	if(error){
            console.log(error);
	}else{
            console.log('Message sent: ' + info.response);
	}
    });
    










module.exports = 
