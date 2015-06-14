angular.module('whiteboard').factory('userService', ['$http', '$q', function($http, $q) {
	'use strict';

	var loginLogoutObservers = [];

	var notifyLoginLogoutObservers = function() {
		_.forEach(loginLogoutObservers, function(callback) {
			callback();
		});
	};

	var currentUser = null;
	
	$http.get('/backend/rest/users/whoami').success(function(user) {
		currentUser = user;
		notifyLoginLogoutObservers();
	});

	return {

		registerLoginLogoutObserver: function(callback) {
			loginLogoutObservers.push(callback);
		},

		unregisterLoginLogoutObserver: function(callback) {
			_.remove(loginLogoutObservers, callback);
		},

		getCurrentUser: function() {
			return currentUser;
		},

		login: function(username, password) {
			var deferred = $q.defer();
			$http.post('/backend/rest/users/login', {
				username: username,
				password: password
			}).success(function(user) {
				currentUser = user;
				notifyLoginLogoutObservers();
				deferred.resolve();
			}).error(function() {
				currentUser = null;
				notifyLoginLogoutObservers();
				deferred.reject();
			});
			return deferred.promise;
		},

		logout: function() {
			currentUser = null;
			notifyLoginLogoutObservers();

			$http.post('/backend/rest/users/logout');
		},

		register: function(user) {
			var deferred = $q.defer();
			$http.post('/backend/rest/users/register', user).success(function() {
				deferred.resolve();
			}).error(function() {
				deferred.reject();
			});
			return deferred.promise;
		}

	};
}]);
