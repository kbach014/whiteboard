angular.module('whiteboard').factory('whiteboardService', ['$http', '$q', '$interval', function($http, $q, $interval) {
	'use strict';

	var socket;
	var messageCallback = _.noop();
	var errorCallback = _.noop();
	var eventQueue = [];
	var queueDrainer = null;
	var drainQueue = function() {
		if (socket && socket.readyState === WebSocket.OPEN && !_.isEmpty(eventQueue)) {
			var toSend = eventQueue;
			eventQueue = [];
			socket.send(JSON.stringify(toSend));
		}
	};

	return {

		connect: function(whiteboardId) {
			this.close();
			var deferred = $q.defer();
			socket = new WebSocket('ws://localhost:8080/backend/drawings/' + whiteboardId);

			socket.onerror = function() {
				deferred.reject();
			};

			socket.onmessage = function(message) {
				messageCallback(JSON.parse(message.data));
			};

			socket.onopen = function() {
				socket.onerror = _.noop();
				queueDrainer = $interval(drainQueue, 100, false);
				deferred.resolve();
	    };

			return deferred.promise;
		},

		setErrorCallback: function(callback) {
			if (_.isFunction(callback)) {
				errorCallback = callback;
			} else {
				errorCallback = _.noop();
			}

			if (socket) {
				socket.onerror = errorCallback;
			}
		},

		setMessageCallback: function(callback) {
			if (_.isFunction(callback)) {
				messageCallback = callback;
			} else {
				messageCallback = _.noop();
			}

			if (socket) {
				socket.onmessage = function(message) {
					messageCallback(JSON.parse(message.data));
				};
			}
		},

		scheduleDrawEvent: function(drawEvent) {
			eventQueue.push(drawEvent);
		},

		close: function() {
			if (queueDrainer) {
				$interval.cancel(queueDrainer);
				queueDrainer = null;
			}
			if (socket && socket.readyState === WebSocket.OPEN) {
				socket.close();
			}
		}

	};
}]);
