'use strict';

const Hapi = require('hapi'),
	config = require('./config/server'),
	routes = require('./config/routes'),
	database = require('./config/queries'),
	SocketIO = require('socket.io'),
	nodemailer = require('nodemailer');

const server = new Hapi.Server();
server.connection({
	host: config.server.host,
	port: config.server.port
});
server.route(routes);

/** Global Variables */
var key;
var promise = database.getEncryptionPassword();
promise.catch(function(err) {
			socket.emit('Error', err);
		}).then(function(psw) { key = psw.password; });

/** Nodemailer Definition */
var transporter;
var emailPSW;
var promise = database.getEmailPSW();
promise.catch(function(err) {
			socket.emit('Error', err);
		}).then(function(psw) {
			transporter = nodemailer.createTransport({
				service: 'Gmail',
				auth: {
					user: 'CorChurchIrvine@gmail.com',
					pass: psw.password
				},
				/** Logging and Debugging */
				//logger: true,
				//debug: true
			}, {
				from: 'COR Church App <corchurchirvine@gmail.com>',
		});
});

const io = SocketIO.listen(server.listener);
/** Socket Functionality */
io.sockets.on('connection', function(socket) {
	
	socket.on('send message', function(data) {
		io.emit('new message', data);
	});
	
	socket.on('create user', function(data) {
		var password = encrypt(data.password);
		var user = database.createUser(data, password);
		user.save(function(err) {
			if(err) { return socket.emit('Error', err); }			
		});
	});
	
	socket.on('signup', function(data) {
		var promise = database.getUser(data.email);
		promise.catch(function(err) { socket.emit('Error', err); }).then(function(user) {
			if(user == null) { return socket.emit('user exist', false); }
			else { return socket.emit('user exist', true); }
		});
	});
	
	socket.on('check user', function(data) {
		var password = encrypt(data.password);
		var promise = database.checkAccount(data.email, password);
		promise.catch(function(err) { socket.emit('Error', err); }).then(function(user) {
			if(user != null) {
				socket.emit('user exists', true);
				// TODO: Remove the sending of users psw via email. Have user reset psw.
				return sendUserPsw(user.name, user.email, decrypt(user.password));
			} else { return socket.emit('not found', "Account does not exist, please signup."); }
		});
	});
	
	socket.on('get groups', function(data) {
		var promise = database.getUser(data.email);
		promise.catch(function(err) { socket.emit('Error', err); }).then(function(user) { 
			return socket.emit('groups', user.groups); });
	});
	
	// TODO: Throw ACK, the update screen with group
	socket.on('add group', function(data) {
		var promise = database.addGroup(data);
		promise.catch(function(err) { socket.emit('Error', err); });
	});
	
	socket.on('login', function(data) {
		var password = encrypt(data.password);
		var promise = database.checkAccount(data.email, password);
		promise.catch(function(err) { socket.emit('Error', err); }).then(function(user) {
			if(user == null) { return socket.emit('not found', "Account does not exist. Check email/password combo."); } 
			else { return socket.emit('check password', true); }
		});
	});
});

server.start(function() {
	console.log('Server running at: ', server.info.uri);
});

/** 
 * Encrypt user's password 
 * @param {String} psw
 * @return {String} encrypted
 */
function encrypt(psw) {
	const crypto = require('crypto');
	const algorithm = 'aes256';
	const cipher = crypto.createCipher(algorithm, key);
	var encrypted = cipher.update(psw, 'utf8', 'hex') + cipher.final('hex');
	return encrypted;
}

/** 
 * Decrypt user's password 
 * @param {String} psw
 * @return {String} decrypted
 */
function decrypt(psw) {
	const crypto = require('crypto');
	const algorithm = 'aes256';
	const decipher = crypto.createDecipher(algorithm, key);
	var decrypted = decipher.update(psw, 'hex', 'utf8') + decipher.final('utf8');
	return decrypted;
}

/** 
 * Send User's account password via email 
 * @param {String} name
 * @param {String} email
 * @param {String} psw
 */
function sendUserPsw(name, email, psw) {
	// Message object
	var message = {

		// Comma separated list of recipients
		to: '"' + name + '"' + '<' + email + '>',

		// Subject of the message
		subject: 'COR Mobile App Password', //

		// plaintext body
		// Need new moethod for password reset.
		text: 'Please do not reply to this message. This inbox is not monitored.' + 'Your password is ' + psw,

		// HTML body
		html: '<p style="color: blue;"><i>Please do not reply to this message. This inbox is not monitored.</i></p>' +
			'<p>Your password is <b>' + psw + '</b>.</p>',

		// Apple Watch specific HTML body
		watchHtml: '<p><i>Please do not reply to this message. This inbox is not monitored.</i></p>' +
			'<p>Your password is <b>' + psw + '</b>.</p>'
	};
	transporter.sendMail(message, function (err, info) {
		if(err) {
			console.log('Message to  ' + name + ' at ' + email + '. Error is ', err);
		}
		//console.log('Success', 'Message sent successfully!');
	});
}

