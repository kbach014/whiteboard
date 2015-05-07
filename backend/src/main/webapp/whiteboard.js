angular.module('whiteboard', ['ngRoute']);

angular.module('whiteboard').config(['$routeProvider', '$httpProvider', function($routeProvider) {
	'use strict';

	$routeProvider.when('/whiteboard/:id', {
		controller: 'WhiteboardCtrl',
		templateUrl: 'partials/whiteboard.html'
	});

	$routeProvider.otherwise({
		redirectTo: '/whiteboard/42'
	});

}]);
;

angular.module('whiteboard').controller('WhiteboardCtrl', ['$scope', '$routeParams', 'whiteboardService', function($scope, $routeParams, whiteboardService) {
	'use strict';

	if ($routeParams.id) {
		whiteboardService.connect($routeParams.id).then(function() {
			$scope.successMessage = 'Connection established.';
		}, function() {
			$scope.errorMessage = 'Unable to connect to shared whiteboard.';
		});
	}

	$scope.draw = function() {
		whiteboardService.scheduleDrawEvent({
			shapeUuid: '8218c5e7-a950-4ebb-bb3d-7d4987e8c51c',
			eventUuid: '8218c5e7-a950-4ebb-bb3d-7d4987e8c51d',
			shape: 'RECT',
			type: 'START',
			coords: '0, 0, 10, 10'
		});
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
;

angular.module('whiteboard').directive('delegateFocus', [function () {
  'use strict';

  return {
      restrict: 'A',
      scope: {
        delegateFocus: '@',
      },
      link: function ($scope, $element, $attrs) {
        $element.on('focus', function() {
          var target = angular.element('#' + $attrs.delegateFocus);
          if (target) {
            target.focus();
          }
        });
      }
  };
}]);
;

angular.module('whiteboard').factory('whiteboardService', ['$http', '$q', '$interval', function($http, $q, $interval) {
	'use strict';

	var socket;
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

			socket.onerror = function(asd) {
				deferred.reject();
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
