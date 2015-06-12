angular.module('whiteboard').controller('WhiteboardCtrl', ['$scope', '$routeParams', 'whiteboardService', function($scope, $routeParams, whiteboardService) {
	'use strict';

	if ($routeParams.id) {
		whiteboardService.connect($routeParams.id).then(function() {
			$scope.successMessage = 'Connection established.';
		}, function() {
			$scope.errorMessage = 'Unable to connect to shared whiteboard.';
		});
	}

	$scope.shapes = [];

	$scope.shapetype = 'PATH';

	$scope.updateShape = function(event) {
		whiteboardService.scheduleDrawEvent(event);
		var existingShape = _.findWhere($scope.shapes, {'uuid': event.shape.uuid});
		if (existingShape) {
			// udpate
			_.assign(existingShape, event.shape);
		} else {
			// add
			$scope.shapes.push(event.shape);
		}
	};

	$scope.dismissErrorMessage = function() {
		$scope.errorMessage = null;
	};

	$scope.dismissSuccessMessage = function() {
		$scope.successMessage = null;
	};

}]);
