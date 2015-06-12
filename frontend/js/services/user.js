angular.module('whiteboard').factory('userService', ['$http', '$q', function($http, $q) {
	'use strict';

	var loginLogoutObservers = [];

	var notifyLoginLogoutObservers = function() {
		_.forEach(loginLogoutObservers, function(callback) {
			callback();
		});
	};

	var currentUser = null;

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
			// TODO remove mock shit:
			currentUser = {username: username};
			notifyLoginLogoutObservers();
			
			var deferred = $q.defer();
			$http.post('/backend/rest/users/login', {
				username: username,
				password: password
			}).success(function(user) {
				currentUser = user;
				notifyLoginLogoutObservers();
				deferred.resolve();
			}).error(function() {
				// TODO uncomment:
				//currentUser = null;
				//notifyLoginLogoutObservers();
				deferred.reject();
			});
			return deferred.promise;
		},

		logout: function() {
			currentUser = null;
			notifyLoginLogoutObservers();

			// TODO http logout
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
