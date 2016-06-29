module.exports = [
	{ 
		method: 'GET', 
		path: '/',
		handler: function(request, reply) {
			reply('Welcome to COR Chat!');
		}
	}
];
