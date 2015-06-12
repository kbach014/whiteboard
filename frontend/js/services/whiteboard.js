angular.module('whiteboard').factory('whiteboardService', ['$http', '$q', '$interval', function($http, $q, $interval) {
	'use strict';

	var socket;
	var eventQueue = [];
	var queueDrainer = null;
	var drainQueue = function() {
		if (socket && socket.readyState === WebSocket.OPEN && !_.isEmpty(eventQueue)) {
			var toSend = eventQueue;
			eventQueue = [];
			console.log('SEND', toSend);
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

			socket.onmessage = function(event) {
				console.log('RECEIVE', event.data);
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
				socket.onerror = callback;
			} else {
				socket.onerror = _.noop();
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
