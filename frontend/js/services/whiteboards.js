angular.module('whiteboard').factory('whiteboardsService', ['$http', '$q', function($http, $q) {
	'use strict';

	return {

		findRegisteredWhiteboards: function() {
			var deferred = $q.defer();
			$http.get('/backend/rest/whiteboards/registered').success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		},

		findUnregisteredWhiteboards: function() {
			var deferred = $q.defer();
			$http.get('/backend/rest/whiteboards/unregistered').success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		},

		createWhiteboards: function() {
			var deferred = $q.defer();
			$http.post('/backend/rest/whiteboards', {}).success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		},

		update: function(whiteboard) {
			var deferred = $q.defer();
			$http.put('/backend/rest/whiteboards/' + whiteboard.id, whiteboard).success(deferred.resolve).error(deferred.reject);
			return deferred.promise;
		}

	};
}]);
