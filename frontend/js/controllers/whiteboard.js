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
