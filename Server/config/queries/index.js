	/** Import mongoose module for MongoDB */
	var mongoose = require('mongoose');
	
	/** Import bluebird Promise library for mongoose (ES6-Style) */
	mongoose.Promise = require('bluebird');
	
	
	/** Database Connect */
	mongoose.connect('mongodb://localhost:27017/COR_Chat', function(err) {
		if(err) { console.log(err);	} 
		else { console.log('Connected to MongoDB.'); }
	});
		
	/** Schema Definitions */
	var userSchema = mongoose.Schema({
		name: String,
		email: String,
		password: String,
		picture: String,
		groups: [String]
	});
	var emailSchema = mongoose.Schema({
		email: String,
		password: String
	});
	var encryptSchema = mongoose.Schema({
		name: String,
		password: String
	});

	
	/** Schemas */
	var User = mongoose.model('Users', userSchema);
	var Email = mongoose.model('Email', emailSchema);
	var Encryption = mongoose.model('Encryption', encryptSchema);
	
/** Functions to be exported */
module.exports = {
	/** 
	 * Get encryption password from database 
	 * @return {Object} promise
	 */
	getEncryptionPassword: function() {
		var promise = Encryption.findOne({name: 'password'}).exec();
		return promise;
	},
	
	/** 
	 * Get email account password from database 
	 * @return {Object} promise
	 */
	getEmailPSW: function() {
		var promise = Email.findOne({email: 'corchurchirvine@gmail.com'}).exec();
		return promise;
	},
	
	/**
	 * Create user to be stored in database
	 * @param {Object} data
	 * @return {Object} user
	 */
	createUser: function(data, psw) {
		var user = new User({name: data.name, email: data.email, password: psw, 
			picture: data.picture, groups: ["Messages from Pastor"]});
		return user;
	},
	
	/** 
	 * Get user account and properties 
	 * @param {String} email
	 * @return {Object} promise
	 */
	getUser: function(email) {
		var promise = User.findOne({email: email}).exec();
		return promise;
	},
	
	/**
	 * Add group to user's groups array
	 * @param {Object} data
	 * @return {Object} promise
	 */
	addGroup: function(data) {
		var promise = User.update(
						{ email: data.email },
						{ $push: { groups: data.group } } ).exec();
		return promise;
	},
	
	/** 
	 * Check if password matches email 
	 * @param {String} email
	 * @param {String} psw
	 * @return {Object} promise 
	 */
	checkPassword: function(email, psw) {
		var promise = User.findOne({email: email, password: password}).exec();
		return promise;
	},
	
	/** 
	 * Check if password matches email 
	 * @param {String} email
	 * @param {String} psw
	 * @return {Object} promise 
	 */
	checkAccount: function(email, psw) {
		var promise = User.findOne({email: email, password: psw}).exec();
		return promise;
	}
};
