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

	$scope.shapetype = 'LINE';

	$scope.updateShape = function(shape) {
		console.log('foo', JSON.stringify(shape));
	};

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

/**
 * <canvas whiteboard-event-callback="updateShape(shape)" whiteboard-track-interval="100" whiteboard-shapes="myShapes" whiteboard-shape-type="'line'|'rect'|'text'"/>
 */
angular.module('whiteboard').directive('whiteboardShapes', ['$interval', function($interval) {
  'use strict';
  return {
    restrict: 'A',
    scope: {
      callback: '&whiteboardEventCallback',
      interval: '=whiteboardTrackInterval',
      shapes: '=whiteboardShapes',
      shapeType: '=whiteboardShapeType'
    },
    link: function ($scope, $element, $attrs) {
      var shape = null;
      var ctx = $element[0].getContext('2d');

      var updateShape = function() {
        if (shape !== null) {
          $scope.callback({shape: shape});
        }
      };

      $element.on('mousedown', function(e) {
        switch($scope.shapeType) {
          case 'LINE':
            shape = {shape: 'LINE', points: [{x: e.offsetX, y: e.offsetY}]};
            ctx.beginPath();
            ctx.moveTo(e.offsetX, e.offsetY);
            break;
          case 'RECT':
            ctx.save();
            shape = {shape: 'RECT', p1: {x: e.offsetX, y: e.offsetY}};
            break;
          case 'TEXT':
            shape = {shape: 'TEXT', p1: {x: e.offsetX, y: e.offsetY}};
            break;
          default:
            console.warn('Unsupported shape ', $scope.shape);
        }
      });

      $element.on('mousemove', function(e) {
        if (shape === null) {
          // noop
          return;
        }
        // intermediate update:
        switch(shape.shape) {
          case 'LINE':
            shape.points.push({x: e.offsetX, y: e.offsetY});
            ctx.lineTo(e.offsetX, e.offsetY);
            ctx.stroke();
            break;
          case 'RECT':
            shape.p2 = {x: e.offsetX, y: e.offsetY};
            ctx.restore();
            ctx.fillRect(Math.min(shape.p1.x, shape.p2.x), Math.min(shape.p1.y, shape.p2.y), Math.abs(shape.p1.x - shape.p2.x), Math.abs(shape.p1.y - shape.p2.y));
            break;
          default:
            //no-op
        }
      });

      $element.on('mouseup', function(e) {
        if (shape === null) {
          // noop
          return;
        }
        // final update:
        switch(shape.shape) {
          case 'LINE':
            shape.points.push({x: e.offsetX, y: e.offsetY});
            ctx.lineTo(e.offsetX, e.offsetY);
            ctx.stroke();
            break;
          case 'RECT':
            shape.p2 = {x: e.offsetX, y: e.offsetY};
            break;
          default:
            //no-op
        }
        // callback and cleanup:
        updateShape(shape);
        shape = null;
      });

      $element.on('mouseout', function() {
        if (shape !== null) {
          // TODO: cancel
          console.info('shape canceled');
          shape = null;
        }
      });

      // interval based updates:
      var timer = $interval(updateShape, $scope.interval);
      $element.on('$destroy', function() {
        $interval.cancel(timer);
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
