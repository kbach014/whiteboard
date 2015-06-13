angular.module('whiteboard').controller('WhiteboardCtrl', ['$scope', '$routeParams', 'whiteboardService', function($scope, $routeParams, whiteboardService) {
	'use strict';

	$scope.shapes = [];

	$scope.shapetype = 'PATH';

	var addOrUpdateShape = function(shape) {
		console.log('updating shape', shape);
		var existingShape = _.findWhere($scope.shapes, {'uuid': shape.uuid});
		if (existingShape) {
			// udpate
			_.assign(existingShape, shape);
		} else {
			// add
			$scope.shapes.push(shape);
		}
	};

	if ($routeParams.id) {
		whiteboardService.connect($routeParams.id).then(function() {
			$scope.successMessage = 'Connection established.';
			whiteboardService.setReceiverCallback(function(remoteDrawEvent) {
				console.log('received', remoteDrawEvent);
				addOrUpdateShape(remoteDrawEvent.shape);
			});
		}, function() {
			$scope.errorMessage = 'Unable to connect to shared whiteboard.';
		});
	} else {
		$scope.errorMessage = 'Ungültiges Whiteboard.';
	}

	$scope.onDraw = function(event) {
		whiteboardService.scheduleDrawEvent(event);
		addOrUpdateShape(event.shape);
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
